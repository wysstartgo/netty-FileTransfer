package com.rt.platform.infosys.resource.common.model;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * <pre>
 *
 * 【标题】: 加载properties的工具类
 * 【描述】: 加载properties，考虑到需要加载多个properties的情况，所以这里提供的都是非静态方法，使用时需要构造一个实例;只有获取的操作，是线程安全的
 * 【版权】: 润投科技
 * 【作者】: wuys
 * 【时间】: 2017/6/22 10:11
 * </pre>
 */
@ThreadSafe
public class PropertiesFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesFile.class);

    private Properties properties = new Properties();

    private PropertiesFile(){}

    /**
     * 用于构建一个实例
     * @param classpath 资源的路径,如果不是classpath需要自己实现读取的方法
     * @return 一个实例
     */
    public static PropertiesFile buildInstance(String classpath){
        PropertiesFile propertiesFile = new PropertiesFile();
        propertiesFile.loadFromClasspath(classpath);
        return propertiesFile;
    }

    /**
     * 从classpath中加载properties文件
     * @author wuys
     * @param classpath
     * @throws IOException
     */
    private void loadFromClasspath(String classpath){
        ClassLoader classLoader = PropertiesFile.class.getClassLoader();
        URL resource = classLoader.getResource(classpath);
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            LOGGER.error("加载配置文件出现异常!",e);
        }
    }

    /**
     *
     * @param key 要获取的key
     * @param defaultValue  得到的值
     * @return 从properties中获取到的值
     */
    public String getString(String key,String defaultValue){
        String value = (String) properties.get(key);
        if(Strings.isNullOrEmpty(value)){
            return defaultValue;
        }
        return value;
    }

    /**
     * 无默认值的实现方式
     * @param key
     * @return 获取到的值
     */
    public String getString(String key){
        return (String) properties.get(key);
    }

    /**
     * 获取Integer的值
     * @param key
     * @return
     */
    public Integer getInteger(String key){
        return Integer.valueOf(key);
    }

    /**
     * @param key
     * @param defaultValue
     * @return 获取到的值或默认值
     */
    public Integer getInteger(String key,Integer defaultValue){
       Integer value = defaultValue;
       try{
           value = Integer.valueOf(properties.getProperty(key));
       }catch (NumberFormatException e){
           LOGGER.error("数据格式化异常",e);
       }
       return value;
    }

    /**
     * 获取布尔类型的值
     * @param key 键
     * @param defaultValue
     * @return 从properties中获取到的值
     */
    public boolean getBoolean(String key, boolean defaultValue){
        String value = (String) properties.get(key);
        if(Strings.isNullOrEmpty(value)){
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }





}
