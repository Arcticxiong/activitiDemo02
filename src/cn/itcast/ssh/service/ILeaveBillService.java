package cn.itcast.ssh.service;

import java.util.List;

import cn.itcast.ssh.domain.LeaveBill;



public interface ILeaveBillService {

	List<LeaveBill> findLeaveBill(LeaveBill leaveBill);

	void saveLeaveBill(LeaveBill leaveBill);

	LeaveBill findLeaveBillByID(Long id);

	void deleteLeaveBillByID(Long id);

}
