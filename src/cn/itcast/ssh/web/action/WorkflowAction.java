package cn.itcast.ssh.web.action;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.struts2.ServletActionContext;

import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.ILeaveBillService;
import cn.itcast.ssh.service.IWorkflowService;
import cn.itcast.ssh.utils.SessionContext;
import cn.itcast.ssh.utils.ValueContext;
import cn.itcast.ssh.web.form.WorkflowBean;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class WorkflowAction extends ActionSupport implements ModelDriven<WorkflowBean> {

	private WorkflowBean workflowBean = new WorkflowBean();
	
	@Override
	public WorkflowBean getModel() {
		return workflowBean;
	}
	
	private IWorkflowService workflowService;
	
	private ILeaveBillService leaveBillService;

	public void setLeaveBillService(ILeaveBillService leaveBillService) {
		this.leaveBillService = leaveBillService;
	}

	public void setWorkflowService(IWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * 部署管理首页显示
	 * @return
	 */
	public String deployHome(){
		//1：查询部署对象的集合列表，返回List<Deployment>
		List<Deployment> depList = workflowService.findDeploymentList();
		//2：查询流程定义的集合列表，返回List<ProcessDefinition>
		List<ProcessDefinition> pdList = workflowService.findPDList();
		ValueContext.putValueContext("depList", depList);
		ValueContext.putValueContext("pdList", pdList);
		return "deployHome";
	}
	
	/**
	 * 发布流程
	 * @return
	 */
	public String newdeploy(){
		//部署流程定义
		workflowService.saveNewDeploy(workflowBean);
		return "list";
	}
	
	/**
	 * 删除部署信息
	 */
	public String delDeployment(){
		//1：获取部署对象ID
		String deploymentId = workflowBean.getDeploymentId();
		//2：使用部署对象ID，级联删除对应的流程定义的信息
		workflowService.deleteDeploymentByID(deploymentId);
		return "list";
	}
	
	/**
	 * 查看流程图，不需要返回到页面
	 * @throws Exception 
	 */
	public String viewImage() throws Exception{
		//1：获取部署对象ID和资源图片名称
		String deploymentId = workflowBean.getDeploymentId();
		String imageName = workflowBean.getImageName();
		//2：使用部署对象ID和资源图片名称，获取输入流，其中输入流中存放的就是图片的资源
		InputStream in = workflowService.findImageInputStream(deploymentId,imageName);
		//3：将图片资源写到输出流（从响应对象中获取）
		OutputStream out = ServletActionContext.getResponse().getOutputStream();
		for(int b=-1;(b=in.read())!=-1;){
			out.write(b);
		}
		out.close();
		in.close();
		return NONE;
	}
	
	// 启动流程
	public String startProcess(){
		//启动流程，改变业务表的状态（从0变成1）
		workflowService.saveStartProcess(workflowBean);
		return "listTask";
	}
	
	
	
	/**
	 * 任务管理首页显示
	 * @return
	 */
	public String listTask(){
		//1：从Session中获取当前登录人
		String name = SessionContext.get().getName();
		//2：使用当前登录人作为查询条件，查询正在执行的任务表，获取当前办理人能够办理的任务集合，返回List<Task>
		List<Task> list = workflowService.findTaskListByName(name);
		ValueContext.putValueContext("list", list);
		return "task";
	}
	
	/**
	 * 打开任务表单
	 */
	public String viewTaskForm(){
		//获取任务id
		String taskId = workflowBean.getTaskId();
		//url的值，从流程图中任务节点的Form key中获取传递的url
		String url = workflowService.findFormKeyByTaskId(taskId);
		//传递任务id
		url += "?taskId="+taskId;
		//传递url
		ValueContext.putValueContext("url", url);
		return "viewTaskForm";
	}
	
	// 准备表单数据
	public String audit(){
		//获取任务ID
		String taskId = workflowBean.getTaskId();
		/**一：使用任务ID，查询请假单的信息，然后进行页面的表单回显*/
		LeaveBill bill = workflowService.findLeaveBillByTaskId(taskId);
		ValueContext.putValueStack(bill);
		/**二：获取当前任务完成之后的输出连线的集合名称，将集合名称存到List<String>中*/
		List<String> outcomeList = workflowService.findOutComeListByTaskID(taskId);
		ValueContext.putValueContext("outcomeList", outcomeList);
		/**三：查询流程中每个任务完成之后的批注信息集合，返回List<Comment>*/
		List<Comment> commentList = workflowService.findCommontListByTaskId(taskId);
		ValueContext.putValueContext("commentList", commentList);
		return "taskForm";
	}
	
	/**
	 * 提交任务
	 */
	public String submitTask(){
		workflowService.saveSubmitTask(workflowBean);
		return "listTask";
	}
	
	/**
	 * 查看当前流程图（查看当前活动节点，并使用红色的框标注）
	 */
	public String viewCurrentImage(){
		//获取任务ID
		String taskId = workflowBean.getTaskId();
		//1：使用任务ID，查询流程图，返回流程定义对象，封装部署对象ID和资源图片名称
		ProcessDefinition pd = workflowService.findProcessDefinitionByTaskId(taskId);
		//* 获取部署对象ID和资源图片名称
		String deploymentId = pd.getDeploymentId();
		String imageName = pd.getDiagramResourceName();
		ValueContext.putValueContext("deploymentId", deploymentId);
		ValueContext.putValueContext("imageName", imageName);
		//2：使用任务ID，查询当前活动的坐标，返回Map<String,Object>
		Map<String, Object> map = workflowService.findCoordingByTaskId(taskId);
		ValueContext.putValueContext("map", map);
		return "image";
	}
	
	// 查看历史的批注信息
	public String viewHisComment(){
		Long id = workflowBean.getId();
		//1：使用请假单ID，查询请假单的信息，进行表单回显
		LeaveBill bill = leaveBillService.findLeaveBillByID(id);
		ValueContext.putValueStack(bill);
		//2：使用请假单ID，查询act_hi_comment表，获取当前流程执行的时候，填写批注信息，返回List<Commont>
		List<Comment> list = workflowService.findCommonListByLeaveBillId(id);
		ValueContext.putValueContext("commentList", list);
		return "viewHisComment";
	}
}
