package com.manage.club.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.manage.club.entity.Club;

import java.util.List;

public interface ClubService extends IService<Club> {

    IPage<Club> listByKeyword(Integer pageNum, Integer pageSize, String keyword);

    Club getDetail(Long clubId, Long userId);

    boolean joinClub(Long userId, Long clubId);

    boolean quitClub(Long userId, Long clubId);

    List<Club> getJoinList(Long userId);

    boolean auditClub(Long clubId, Integer status, String reason);

    List<Club> getAuditList();

    // 社团管理员相关
    List<Club> getManagedClubs(Long userId);

    boolean isClubAdmin(Long userId, Long clubId);
    //更新状态
    boolean updateJoinStatus(Long clubId, Long userId, Integer status);
    List<Club> getAuditListWithCreator();
}