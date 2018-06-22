package cn.itcast.ssh.web.action;

import java.util.List;

import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.ILeaveBillService;
import cn.itcast.ssh.utils.ValueContext;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class LeaveBillAction extends ActionSupport implements ModelDriven<LeaveBill> {

	private LeaveBill leaveBill = new LeaveBill();
	
	@Override
	public LeaveBill getModel() {
		return leaveBill;
	}
	
	private ILeaveBillService leaveBillService;

	public void setLeaveBillService(ILeaveBillService leaveBillService) {
		this.leaveBillService = leaveBillService;
	}

	/**
	 * 请假管理首页显示
	 * @return
	 */
	public String home(){
		//2：查询请假单表，返回List<LeaveBill>，遍历到页面上
		List<LeaveBill> list = leaveBillService.findLeaveBill(leaveBill);
		//放置到上下文的对象中
		ValueContext.putValueContext("list", list);
		return "home";
	}
	
	/**
	 * 添加请假申请
	 * @return
	 */
	public String input(){
		//通过主键ID判断，判断是新增还是修改
		Long id = leaveBill.getId();
		//此时表示修改
		if(id!=null){
			//1：使用请假单ID，查询请假单的信息，返回LeaveBill对象
			LeaveBill bill = leaveBillService.findLeaveBillByID(id);
			//2：放置到栈顶，完成表单回显
			ValueContext.putValueStack(bill);
		}
		return "input";
	}
	
	/**
	 * 保存/更新，请假申请
	 * 
	 * */
	public String save() {
		//保存请假单
		leaveBillService.saveLeaveBill(leaveBill);
		return "save";
	}
	
	/**
	 * 删除，请假申请
	 * 
	 * */
	public String delete(){
		//获取请假单
		Long id = leaveBill.getId();
		leaveBillService.deleteLeaveBillByID(id);
		return "save";
	}
	
}
