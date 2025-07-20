package org.example.spring;

import org.example.spring.anntation.*;
import org.example.spring.itf.BeanNameAware;
import org.example.spring.itf.BeanPostProcessor;
import org.example.spring.itf.InitializeBean;

import java.io.File;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: pjpp
 * @Date: 2025/7/90 15:22
 * @Description: spring-demo-context
 */
public class DemoAnnotationApplicationContext {
    private  Class<?>configClass;//配置类

    private ConcurrentHashMap<String, Object>singleBeanMap = new ConcurrentHashMap<>();//单例bean

    private ConcurrentHashMap<String, BeanDefinition>defineMap = new ConcurrentHashMap<>();//bean定义

    List<BeanPostProcessor>processorList = new ArrayList<>();


    public DemoAnnotationApplicationContext(Class<?> configClass){
        this.configClass = configClass;

        if(!configClass.isAnnotationPresent(ComponentScan.class))return;
        //解析配置类
        parseConfig(configClass);
        //创建单例bean
        for(var define : defineMap.entrySet()){
            String beanName = define.getKey();
          //  System.out.println("create bean: " + beanName);
            BeanDefinition beanDefinition = define.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName);
                singleBeanMap.put(beanName, bean);
            }
        }
        //解析类中其他注解
        //单例bean有加载顺序,放在此处处理
        //解析单例bean中@Autowired注解
        for(var define : defineMap.entrySet()){
            if(!define.getValue().getScope().equals("singleton"))continue;
            String beanName = define.getKey();
            Class<?>clazz = define.getValue().getBeanClass();
            for(var field : clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    try {
                        field.set(singleBeanMap.get(beanName), getBean(field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }


    //创建bean
    Object createBean(String beanName){

        BeanDefinition beanDefinition = defineMap.get(beanName);
        try {
            Class<?>clazz = beanDefinition.getBeanClass();
            Object instance = clazz.getDeclaredConstructor().newInstance();
            //prototype 类型的bean没有加载顺序,放在此处处理
            if(!beanDefinition.getScope().equals("singleton")){
                for(var field : clazz.getDeclaredFields()){
                    if(field.isAnnotationPresent(Autowired.class)){
                        field.setAccessible(true);
                        field.set(instance, getBean(field.getType()));
                    }
                }
            }
            //Aware回调
            if (instance instanceof BeanNameAware)((BeanNameAware)instance).setBeanName(beanName);

            //初始化前调用
            for(var processor : processorList)instance = processor.postProcessBeforeInitialization(instance, beanName);

            //初始化bean
            if(instance instanceof InitializeBean)((InitializeBean)instance).afterPropertiesSet();

            //初始化后调用
            for(var processor : processorList)instance = processor.postProcessAfterInitialization(instance, beanName);

           return instance;

        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //解析配置类
    private void parseConfig(Class<?> configClass) {
        //获取ComponentScan注解
        ComponentScan scan = configClass.getAnnotation(ComponentScan.class);
        //获取扫描路径
        String[]paths = Arrays.stream(scan.value()).map(path -> path.replace(".", "\\").replace("*", "")).toArray(String[]::new);
      //  System.out.println(Arrays.toString(paths));
        //获取类加载器
        ClassLoader classLoader = DemoAnnotationApplicationContext.class.getClassLoader();
        //获取资源路径(path暂时只识别单个路径)
        URL resource = classLoader.getResource(paths[0]);
        String basePath = Objects.requireNonNull(resource).getFile().replace("%5c", "\\");
        File file = new File(basePath);

        List<String>classList =new ArrayList<>();
        try {
            //递归扫描
            scanFile(file, Component.class, classList);
            //classList.forEach(System.out::println);
            //对所有Bean进行解析
            for(String className : classList){
                BeanDefinition beanDefinition = new BeanDefinition();
                Class<?>clazz = Class.forName(className);
                //判断是否实现BeanPostProcessor接口
                if(BeanPostProcessor.class.isAssignableFrom(clazz))processorList.add((BeanPostProcessor) clazz.getConstructor().newInstance());

                //解析bean上的注解
                Scope scopeAnno = getMetaAnnotation(clazz, Scope.class);
                beanDefinition.setBeanClass(clazz);
                if(scopeAnno!= null){
                   String scope = scopeAnno.value();
                   if(scope.equals("singleton")){
                       beanDefinition.setScope("singleton");
                   }else{
                       beanDefinition.setScope("prototype");
                   }
                }else{
                    beanDefinition.setScope("singleton");
                }
                //System.out.println("scan class: ");
                //获取自定义bean名称
                Component componentAnno = getMetaAnnotation(clazz, Component.class);
                if(!componentAnno.name().equals("")){
                    defineMap.put(componentAnno.name(), beanDefinition);
                }else{
                    String defaultName = className.substring(className.lastIndexOf(".") + 1);
                    defineMap.put(defaultName, beanDefinition);
                }


            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName){
        if(defineMap.containsKey(beanName)){
            if(defineMap.get(beanName).getScope().equals("singleton")){
                return singleBeanMap.get(beanName);
            }
            return createBean(beanName);
        }else{
            throw new RuntimeException("no bean named " + beanName);
        }
    }

    public <T>T getBean(Class<T>beanClass){
        for(var define : defineMap.entrySet()){
           if(!define.getValue().getBeanClass().equals(beanClass)&& !beanClass.isAssignableFrom(define.getValue().getBeanClass()))continue;
           if(define.getValue().getScope().equals("singleton")){
              // System.out.println("get bean: " + define.getKey());
               return (T)singleBeanMap.get(define.getKey());
           }
           return (T)createBean(define.getKey());
        }
        return null;
    }
    

    void scanFile(File file, Class<? extends Annotation> applyAnno, List<String>classList) throws ClassNotFoundException {
        if(file.isDirectory()){
            File[] files = file.listFiles();
           // Arrays.stream(files).forEach(System.out::println);
            if(files == null)return;
            for(var f : files){
                scanFile(f, applyAnno, classList);
            }
        }else{
            if(file.getName().endsWith(".class")){
                String className = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("classes")+8).replace(".class", "").replace("\\", ".");
                Class<?>clazz = Class.forName(className);
                if(clazz.isAnnotation())return;
                //System.out.println(clazz.getName());
                if(hasMetaAnnotation(clazz, applyAnno))classList.add(className);
            }
        }
    }

    /**
     * 递归判断clazz是否有metaAnno注解
     * @Author: pjpp
     * */

    boolean hasMetaAnnotation(Class<?> clazz, Class<? extends Annotation> metaAnno){
        for(var anno : clazz.getAnnotations()){
           if(anno.annotationType().getPackage().getName().startsWith("java.lang.annotation"))continue;
         //  if(clazz.equals(Configuration.class))System.out.println(clazz.getName());
          if (anno.annotationType().equals(metaAnno) || hasMetaAnnotation(anno.annotationType(), metaAnno)){
              return true;
          }
        }
        return false;
    }

    <T> T getMetaAnnotation(Class<?>clazz, Class<? extends Annotation> metaAnno){
        if(clazz.isAnnotationPresent(metaAnno))return (T)clazz.getAnnotation(metaAnno);
        for(var anno : clazz.getAnnotations()){
            if(anno.annotationType().getPackage().getName().startsWith("java.lang.annotation"))continue;
            T t = getMetaAnnotation(anno.annotationType(), metaAnno);
            if(t!= null)return t;
        }
        return null;
    }

}
