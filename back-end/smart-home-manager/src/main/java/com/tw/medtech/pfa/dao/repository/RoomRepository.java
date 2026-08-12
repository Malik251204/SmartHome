package com.tw.medtech.pfa.dao.repository;

import com.tw.medtech.pfa.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByUsers_Id(Long userId);
}