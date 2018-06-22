package cn.itcast.ssh.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.web.form.WorkflowBean;



public interface IWorkflowService {

	void saveNewDeploy(WorkflowBean workflowBean);

	List<Deployment> findDeploymentList();

	List<ProcessDefinition> findPDList();

	InputStream findImageInputStream(String deploymentId, String imageName);

	void deleteDeploymentByID(String deploymentId);

	void saveStartProcess(WorkflowBean workflowBean);

	List<Task> findTaskListByName(String name);

	String findFormKeyByTaskId(String taskId);

	LeaveBill findLeaveBillByTaskId(String taskId);

	List<String> findOutComeListByTaskID(String taskId);

	void saveSubmitTask(WorkflowBean workflowBean);

	List<Comment> findCommontListByTaskId(String taskId);

	List<Comment> findCommonListByLeaveBillId(Long id);

	ProcessDefinition findProcessDefinitionByTaskId(String taskId);

	Map<String, Object> findCoordingByTaskId(String taskId);

	

}
