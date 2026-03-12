package com.ecampus.repository;

import java.util.List;

import com.ecampus.model.Egcrstt1;
import com.ecampus.model.Egcrstt1Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Egcrstt1Repository extends JpaRepository<Egcrstt1, Egcrstt1Id> {

    List<Egcrstt1> findByStudIdAndTcridIn(Long studId, List<Long> tcrIds);
}