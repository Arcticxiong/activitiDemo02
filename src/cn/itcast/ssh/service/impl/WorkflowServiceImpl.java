package cn.itcast.ssh.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.IWorkflowService;
import cn.itcast.ssh.utils.SessionContext;
import cn.itcast.ssh.web.form.WorkflowBean;

public class WorkflowServiceImpl implements IWorkflowService {
	/**请假申请Dao*/
	private ILeaveBillDao leaveBillDao;
	
	private RepositoryService repositoryService;
	
	private RuntimeService runtimeService;
	
	private TaskService taskService;
	
	private FormService formService;
	
	private HistoryService historyService;
	
	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}
	
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
	
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	
	/**完成流程定义的部署*/
	@Override
	public void saveNewDeploy(WorkflowBean workflowBean) {
		try {
			//1：获取页面的流程文件File
			File file = workflowBean.getFile();
			//获取流程的名称
			String filename = workflowBean.getFilename();
			//2：将File类型的文件，转换成ZIPInputStream流
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
			//3：使用工作流的API，完成部署流程定义
			repositoryService.createDeployment()//
						.name(filename)//定义流程的名称
						.addZipInputStream(zipInputStream)//zip输入流部署
						.deploy();//完成部署
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**查询部署对象的集合*/
	@Override
	public List<Deployment> findDeploymentList() {
		List<Deployment> list = repositoryService.createDeploymentQuery()//
						.orderByDeploymenTime().asc()//按照时间排序
						.list();
		return list;
	}
	
	/**查询流程定义的集合*/
	@Override
	public List<ProcessDefinition> findPDList() {
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()//
						.orderByProcessDefinitionVersion().asc()//按照版本的升序排列
						.list();
		return list;
	}
	
	/**使用部署ID和资源图片名称，获取图片输入流*/
	@Override
	public InputStream findImageInputStream(String deploymentId,
			String imageName) {
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}
	
	/**使用部署对象ID，删除部署信息*/
	@Override
	public void deleteDeploymentByID(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId, true);
	}
	
	/**启动流程，改变业务表的状态（从0变成1）*/
	@Override
	public void saveStartProcess(WorkflowBean workflowBean) {
		//获取到请假单ID
		Long id = workflowBean.getId();
		//1：如果流程启动，改变请假单表的state的状态从0（初始录入）变成1（审核中）
		LeaveBill leaveBill = leaveBillDao.findLeaveBillByID(id);
		leaveBill.setState(1);//更新状态
		
		//2：获取LeaveBill的对象名称，使用对象的名称启动流程实例
		String key = leaveBill.getClass().getSimpleName();
		
		//3：在启动流程实例之前
		  //（1）使用流程变量指定任务的办理人，userID表示流程变量的名称，流程变量的值就是当前登录人
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("userID", SessionContext.get().getName());//唯一，使用ID也可以使用登录名
		//流程启动的时候，保证让流程关联审核的业务。
		  /***(2)：存放String类型的值，但是String类型的值一定要体现出请假单的业务，而且是第几条数据
		   *    String类型的格式：LeaveBill.2。它就表示请假单对象的id为2个值。		   * 
		   */
		//存放业务的参数
		String businessValue = key+"."+id;
		variables.put("objID", businessValue);
		/**
		 * 方案二：使用Activiti提供的一个字段（在正在执行的执行对象表和历史的流程实例表中存在。目的是让流程实例关联业务）
		 	* 该字段存放的是String类型的值
		 	* String类型的格式：LeaveBill.2。它就表示请假单对象的id为2个值。		 
		 	* 在启动流程实例的时候，将值存放到startProcessInstanceByKey参数的第2个位置  * 
		 */
		//启动流程
		runtimeService.startProcessInstanceByKey(key,businessValue,variables);
	}
	
	/**使用当前办理人，查询当前办理人对应的任务的集合*/
	@Override
	public List<Task> findTaskListByName(String name) {
		List<Task> list = taskService.createTaskQuery()//
					.taskAssignee(name)//指定个人任务的办理人集合
					.orderByTaskCreateTime().asc()//按照时间升序排列
					.list();
		return list;
	}
	
	//使用任务id，查询form key中的存放的url
	@Override
	public String findFormKeyByTaskId(String taskId) {
		TaskFormData formData = formService.getTaskFormData(taskId);
		String url = formData.getFormKey();
		return url;
	}

	/**使用任务ID，查询请假单的信息*/
	@Override
	public LeaveBill findLeaveBillByTaskId(String taskId) {
		//1：有了任务ID，一定有任务对象，查询流程实例ID
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//主键ID查询
					.singleResult();
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//2：有了流程实例ID，查询正在执行的执行对象表，获取BUSINESS_KEY的值
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
					.processInstanceId(processInstanceId)//流程实例ID查询
					.singleResult();
		//获取BUSINESS_KEY的值（格式：LeaveBill.2）
		String businessKey = pi.getBusinessKey();
		//判断是否是请假流程
		String [] arrays = businessKey.split("\\.");//点 需要转义 如果用其他符号 就不用转义了
		String business = arrays[0];
		String businessId = arrays[1];
		//请假流程
		LeaveBill leaveBill = null;
		if("LeaveBill".equals(business)){
			//3：从使用业务的主键ID，获取业务的信息
			leaveBill = leaveBillDao.findLeaveBillByID(Long.parseLong(businessId));
		}
		//费用报销流程.....
		return leaveBill;
	}

	
	/**取当前任务完成之后的输出连线的集合名称，将集合名称存到List<String>中*/
	@Override
	public List<String> findOutComeListByTaskID(String taskId) {
		List<String> outcomeList = new ArrayList<String>();
		//1：有了任务ID，一定有任务对象，查询流程实例ID
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//主键ID查询
					.singleResult();
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//2：有了流程实例ID，查询正在执行的执行对象表，获取当前活动ID的值
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
					.processInstanceId(processInstanceId)//流程实例ID查询
					.singleResult();
		//获取当前活动ID
		String activityId = pi.getActivityId();
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//获取流程定义的实体对象，因为ProcessDefinitionEntity可以对应.bpmn文件的内容
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService
								.getProcessDefinition(processDefinitionId);
		
		//通过流程定义的实体对象，获取当前任务（正在执行的任务）完成之后的连线的集合名称
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//获取当前活动对象，需要传递当前活动ID
		//使用当前活动对象，获取当前活动完成之后的连线对象
		List<PvmTransition> list = activityImpl.getOutgoingTransitions();
		if(list!=null && list.size()>0){
			for(PvmTransition pvmTransition:list){
				String value = (String)pvmTransition.getProperty("name");
				//任务完成之后，存在多条连线的情况
				if(StringUtils.isNotBlank(value)){
					outcomeList.add(value);
				}
				//任务完成之后，只存在1条连线的情况
				else{
					outcomeList.add("默认提交");
				}
			}
		}
		return outcomeList;
	}
	
	/**提交审核记录信息，完成审核*/
	@Override
	public void saveSubmitTask(WorkflowBean workflowBean) {
		//获取到的参数
		String taskId = workflowBean.getTaskId();
		//批注信息
		String comment = workflowBean.getComment();
		//完成任务后的连线的名称
		String outcomet = workflowBean.getOutcome();
		//有了任务ID，一定有任务对象，查询流程实例ID
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//主键ID查询
					.singleResult();
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		/**1：完成任务之前，将页面填写的数据，添加到Activiti工作流提供的批注信息表中（act_hi_comment）*/
		/**
		 * 一定要设置当前登录人，为任务的办理人，或者叫做审核人
		 *   * 因为底层代码：
		 *   	String userId = Authentication.getAuthenticatedUserId();
			    CommentEntity comment = new CommentEntity();
			    comment.setUserId(userId);
			 * 如果不设置Authentication.setAuthenticatedUserId(SessionContext.get().getName());
			 * 那么act_hi_comment的userId字段将为null
		 */
		Authentication.setAuthenticatedUserId(SessionContext.get().getName());//设置当前登录人
		taskService.addComment(taskId, processInstanceId, comment);//参数一：任务ID，参数二：流程实例ID，参数三：批注信息
		/**
		 * 2：完成任务之前，
			   如果当前任务的输出连线只有1条，默认离开
			   如果当前任务的输出连线有多条，使用流程变量，指定完成任务之后，需要按照哪条连线执行
			     * 流程变量的名称：message
			     * 流程变量的值：页面传递按钮的value的属性值
		 * */
		Map<String, Object> variables = new HashMap<String,Object>();
		//表示任务完成之后有多条连线
		if(!"默认提交".equals(outcomet)){
			variables.put("message", outcomet);
		}
		/**3：使用任务ID，完成当前任务*/
		taskService.complete(taskId, variables);
		/**4：任务完成之后，到下一个任务的时候，必须要指定任务的办理人，但是已经开放完成（使用类ManagerTaskHandler）*/
		/**5：如果任务完成之后，流程结束，此时需要更新请假单表中的状态从1变成2（从审核中变成审核完成）*/
		//判断流程是否结束，查找正在执行的执行对象表，使用流程实例ID查询，判断是否为空
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
						.processInstanceId(processInstanceId)//使用流程实例ID查询
						.singleResult();
		//表示流程结束
		if(pi==null){
			//获取请假单ID
			Long id = workflowBean.getId();
			LeaveBill leaveBill = leaveBillDao.findLeaveBillByID(id);
			//更新请假单表中的状态从1变成2（从审核中变成审核完成）
			leaveBill.setState(2);
		}
	}

	/**使用任务ID，查询Activiti工作流提供的批注信息，对应的表act_hi_comment*/
	@Override
	public List<Comment> findCommontListByTaskId(String taskId) {
		List<Comment> commentList = new ArrayList<Comment>();
		/**方案一：使用：taskService.getTaskComments(taskId) 这里的taskId和上面参数中的taskId不是同一个 因为这里是查询上一个任务的批注，
		 * 而此时上一个任务已经结束，如果要查询需要到历史表中查询上一个任务的id*/
		//使用当前的任务ID，查询流程实例ID
		Task task = taskService.createTaskQuery()//
				.taskId(taskId)//主键ID查询
				.singleResult();
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
//		//使用流程实例ID，查询历史的任务表
//		List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()//
//					.processInstanceId(processInstanceId)//使用流程实例ID查询
//					.list();
//		if(list!=null && list.size()>0){
//			for(HistoricTaskInstance hti:list){
//				//获取历史的每个任务ID
//				String hTaskId = hti.getId();
//				List<Comment> hCommentlist = taskService.getTaskComments(hTaskId);
//				//每个任务的批注信息，添加commentList中
//				commentList.addAll(hCommentlist);
//			}
//		}
		/**方案二：使用：taskService.getProcessInstanceComments*/
		commentList = taskService.getProcessInstanceComments(processInstanceId);
		return commentList;
	}

	/**2：使用请假单ID，查询act_hi_comment表，获取当前流程执行的时候，填写的批注信息，返回List<Commont>*/
	/**
	 * 查询与历史有关的表，获取数据
		  因为，如果查询正在执行，如果流程结束了，没有数据了，此时查询不到了
		  如果要想流程结束也能查询得到数据，必须查询历史
	 */
	@Override
	public List<Comment> findCommonListByLeaveBillId(Long id) {
		/**方案一：查找流程实例的历史获取流程实例ID*/
		//组织Business_key字段的值（格式：LeaveBill.2）
		LeaveBill leaveBill = leaveBillDao.findLeaveBillByID(id);
		String processInstanceBusinessKey = leaveBill.getClass().getSimpleName()+"."+id;
//		HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()//
//						.processInstanceBusinessKey(processInstanceBusinessKey)//按照BusinessKey查询对应的值
//						.singleResult();
//		String processInstanceId = hpi.getId();
		/**方案二：查询流程变量的历史获取流程实例ID，存放：variables.put("objID", businessValue);其中businessValue的格式：（格式：LeaveBill.2）*/
		HistoricVariableInstance hvi = historyService.createHistoricVariableInstanceQuery()//
						.variableValueEquals("objID", processInstanceBusinessKey)//根据流程变量的名称和流程变量的值查询
						.singleResult();
		String processInstanceId = hvi.getProcessInstanceId();
		//获取批注信息
		List<Comment> commentList = taskService.getProcessInstanceComments(processInstanceId);
		return commentList;
	}

	/**使用任务ID，查询任务对象*/
	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		//* 使用任务ID，查询任务对象，获取流程定义ID(也可以从页面传)
		Task task = taskService.createTaskQuery()//
				.taskId(taskId)//主键ID查询
				.singleResult();
		//流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//* 使用流程定义ID，查询流程定义的对象ProcessDefinition
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()//
					.processDefinitionId(processDefinitionId)//
					.singleResult();
		return pd;
	}

	/**使用任务ID，获取当前任务（正在执行的任务），对应的坐标信息，存放到Map集合中*/
	@Override
	public Map<String, Object> findCoordingByTaskId(String taskId) {
		/**
		 * Map<String,Object>：存放坐标
			  * map集合的key：坐标的名称
			  * map集合的value：坐标的值
		 */
		Map<String, Object> map = new HashMap<String,Object>();
		//从.bpmn文件获取
		//1：有了任务ID，一定有任务对象，查询流程实例ID
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//主键ID查询
					.singleResult();
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//2：有了流程实例ID，查询正在执行的执行对象表，获取当前活动ID的值
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
					.processInstanceId(processInstanceId)//流程实例ID查询
					.singleResult();
		//获取当前活动ID
		String activityId = pi.getActivityId();
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//获取流程定义的实体对象，因为ProcessDefinitionEntity可以对应.bpmn文件的内容
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService
								.getProcessDefinition(processDefinitionId);
		//使用当前活动的ID，查询当前活动的对象
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
		//坐标
		map.put("x", activityImpl.getX());//距离左侧的距离
		map.put("y", activityImpl.getY());//距离上端的距离
		map.put("width", activityImpl.getWidth());//坐标本身的宽度
		map.put("height", activityImpl.getHeight());//坐标本身的高度		
		return map;
	}
	
}


