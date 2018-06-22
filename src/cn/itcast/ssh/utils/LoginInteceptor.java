package cn.itcast.ssh.utils;

import cn.itcast.ssh.domain.Employee;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;


/**
 * 登录验证拦截器
 *
 */
@SuppressWarnings("serial")
public class LoginInteceptor extends MethodFilterInterceptor {

	protected String doIntercept(ActionInvocation invocation) throws Exception {
		//Session有一个URL是没有的，就是登陆的loginAction_login.action是没有的，去掉
		String url = invocation.getProxy().getActionName();
		//此时需要验证Session
		if(!"loginAction_login".equals(url)){
			//获取Session
			Employee employee = SessionContext.get();
			//表示当前用户的Session已经过期，需要定向到登陆页面
			if(employee==null){
				return "login";
			}
		}
		return invocation.invoke();//调用Action的方法
	}

}
