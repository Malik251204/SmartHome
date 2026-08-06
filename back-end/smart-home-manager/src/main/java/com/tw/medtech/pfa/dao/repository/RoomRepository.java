package com.tw.medtech.pfa.dao.repository;

import com.tw.medtech.pfa.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}