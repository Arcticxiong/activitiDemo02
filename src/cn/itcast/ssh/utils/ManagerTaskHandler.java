package cn.itcast.ssh.utils;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.service.IEmployeeService;

/**
 * 员工经理任务分配
 *
 */
@SuppressWarnings("serial")
public class ManagerTaskHandler implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		//* 获取当前登录人
		Employee employee = SessionContext.get();
		//* 通过当前登录人，获取当前登录人对应的审核人的对象
		// 获取当前登录人的姓名
		String name = employee.getName();
		// 以名称作为查询条件，查询用户信息表，获取当前用户的信息
		// 从web容器中获取spring容器的UI想
		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(ServletActionContext.getServletContext());
		IEmployeeService employeeService = (IEmployeeService)ac.getBean("employeeService");
		Employee employeeManager = employeeService.findEmployeeByName(name);
		String managerName = employeeManager.getManager().getName();
		//* 将审核人的办理人，设置到完成任务后下一个任务的办理人中
		delegateTask.setAssignee(managerName);
	}

}
