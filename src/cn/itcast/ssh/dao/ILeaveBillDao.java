package cn.itcast.ssh.dao;

import java.util.List;

import cn.itcast.ssh.domain.LeaveBill;



public interface ILeaveBillDao {

	List<LeaveBill> findLeaveBillByCondition(String condition);

	void save(LeaveBill leaveBill);

	LeaveBill findLeaveBillByID(Long id);

	void update(LeaveBill leaveBill);

	void deleteLeaveBillByID(Long id);


}
