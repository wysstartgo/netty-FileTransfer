package com.rt.platform.infosys.resource.common.constants;

/**
 * <pre>
 *  
 *
 * 【标题】: 资源中心全局相关常量
 * 【描述】:  
 * 【版权】: 润投科技
 * 【作者】: 唐宋  
 * 【时间】: 2017年6月8日 上午9:10:56
 * </pre>
 */
public interface ResourceGlobalConstants {

    /********************************* 通用相关 ************************************************/
    String APPID = "10000011";

    /********************************** 资源过期时间 *********************************************/
    int RESERVE_PERSISTENT_RESOURCE = 1;// 年

    int RESERVE_EXPERIED_PERSISTENT_RESOURCE = 12;// 月

    int RESERVE_TEMP_RESOURCE = 1;// 月

    int RESERVE_EXPERIED_TEMP_RESOURCE = 1;// 月

    /************************************ 错误码 ***********************************************/

    String PARAMETER_INVALIDATE_EXCEPTION_CODE = "1101";// 参数不合法

    String RPC_EXCEPTION_CODE = "1102";//rpc异常

    /************************************资源标识code*******************************************/
    String RESOURCE_OPERATE_SUCCESS = "1200";//资源保存成功
    String RESOURCE_EXIST_CODE = "1201";//资源已经存在
    String RESOURCE_NOT_EXIST_CODE = "1202";//资源不存在

}
