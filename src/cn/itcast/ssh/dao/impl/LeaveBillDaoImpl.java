package cn.itcast.ssh.dao.impl;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.LeaveBill;

public class LeaveBillDaoImpl extends HibernateDaoSupport implements ILeaveBillDao {

	/**使用查询条件， 查询请假单集合（当前人的条件）*/
	@Override
	public List<LeaveBill> findLeaveBillByCondition(String condition) {
		String hql = "from LeaveBill o where 1=1 "+condition;
		List<LeaveBill> list = this.getHibernateTemplate().find(hql);
		return list;
	}
	
	/**保存请假单*/
	@Override
	public void save(LeaveBill leaveBill) {
		this.getHibernateTemplate().save(leaveBill);
	}
	
	/**主键ID，查询请假单*/
	@Override
	public LeaveBill findLeaveBillByID(Long id) {
		return this.getHibernateTemplate().get(LeaveBill.class, id);
	}
	
	/**更新请假单*/
	@Override
	public void update(LeaveBill leaveBill) {
		this.getHibernateTemplate().update(leaveBill);
	}
	
	/**删除请假单*/
	@Override
	public void deleteLeaveBillByID(Long id) {
		//查询
		LeaveBill entity = this.findLeaveBillByID(id);
		//再删除
		this.getHibernateTemplate().delete(entity);
	}
}
