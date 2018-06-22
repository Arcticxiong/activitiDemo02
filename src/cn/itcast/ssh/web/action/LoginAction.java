package cn.itcast.ssh.web.action;

import org.hibernate.Hibernate;

import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.service.IEmployeeService;
import cn.itcast.ssh.utils.SessionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class LoginAction extends ActionSupport implements ModelDriven<Employee> {

	private Employee employee = new Employee();
	
	@Override
	public Employee getModel() {
		return employee;
	}
	
	private IEmployeeService employeeService;

	public void setEmployeeService(IEmployeeService employeeService) {
		this.employeeService = employeeService;
	}


	/**
	 * 登录
	 * @return
	 */
	public String login(){
		//获取登录名
		String name = employee.getName();
		//1：获取页面的登录名，以登录名作为条件，查询用户表，获取唯一对象Employee对象
		Employee employee = employeeService.findEmployeeByName(name);
//		Hibernate.initialize(employee.getManager());//初始化以防懒加载
		//2：将Employee对象放置到Session中
		SessionContext.setUser(employee);
		return "success";
	}
	
	/**
	 * 标题
	 * @return
	 */
	public String top() {
		return "top";
	}
	
	/**
	 * 左侧菜单
	 * @return
	 */
	public String left() {
		return "left";
	}
	
	/**
	 * 主页显示
	 * @return
	 */
	public String welcome() {
		return "welcome";
	}
	
	/**
	 * 退出系统（注销）
	 * @return
	 */
	public String logout(){
		//清空Session
		SessionContext.setUser(null);
		return "login";
	}
}
