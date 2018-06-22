package cn.itcast.ssh.service.impl;

import java.util.List;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.ILeaveBillService;
import cn.itcast.ssh.utils.SessionContext;

public class LeaveBillServiceImpl implements ILeaveBillService {

	private ILeaveBillDao leaveBillDao;

	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}
	
	/**使用查询条件，查询数据结果*/
	@Override
	public List<LeaveBill> findLeaveBill(LeaveBill leaveBill) {
		//组织查询条件
		//1：查询所有的请假单（当前人）
		Employee employee = SessionContext.get();
		String condition = " and o.user.name='"+employee.getName()+"'";
		List<LeaveBill> list = leaveBillDao.findLeaveBillByCondition(condition);
		return list;
	}
	
	/**保存请假单*/
	@Override
	public void saveLeaveBill(LeaveBill leaveBill) {
		//* 使用页面传递的隐藏域ID，判断当前操作是执行save还是执行update
		Long id = leaveBill.getId();
		//  * 存在id，update
		if(id!=null){
			leaveBillDao.update(leaveBill);
		}
		//  * 不存在id，save
		else{
			//1：将页面的值组织成PO对象，将当前登录用户，存放到PO对象的user属性中，执行save的操作
			//从Session中获取当前用户
			Employee employee = SessionContext.get();
			//创建请假单和用户的外键的关联关系
			leaveBill.setUser(employee);
			leaveBillDao.save(leaveBill);			
		}
	}
	
	/**主键ID，查询请假单*/
	@Override
	public LeaveBill findLeaveBillByID(Long id) {
		return leaveBillDao.findLeaveBillByID(id);
	}
	
	/**删除请假单*/
	@Override
	public void deleteLeaveBillByID(Long id) {
		leaveBillDao.deleteLeaveBillByID(id);
	}

}
