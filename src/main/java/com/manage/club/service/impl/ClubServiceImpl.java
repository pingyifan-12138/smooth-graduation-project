package com.manage.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manage.club.entity.Club;
import com.manage.club.entity.UserClub;
import com.manage.club.mapper.ClubMapper;
import com.manage.club.mapper.UserClubMapper;
import com.manage.club.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements ClubService {
    @Autowired
    private ClubMapper clubMapper;
    @Autowired
    private UserClubMapper userClubMapper;

    @Override
    public IPage<Club> listByKeyword(Integer pageNum, Integer pageSize, String keyword) {
        Page<Club> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Club::getName, keyword);
        }
        wrapper.eq(Club::getStatus, 1);
        return clubMapper.selectPage(page, wrapper);
    }

    @Override
    public Club getDetail(Long clubId, Long userId) {
        // 使用 lambda 查询，确保查询所有字段（包括 images）
        Club club = getById(clubId);
        if (club == null) return null;
        // 设置是否已加入状态
        int joinCount = userClubMapper.isJoined(userId, clubId);
        club.setIsJoined(joinCount > 0);
        return club;
    }

    @Override
    public boolean joinClub(Long userId, Long clubId) {
        if (userClubMapper.isJoined(userId, clubId) > 0) {
            return false;
        }
        UserClub userClub = new UserClub();
        userClub.setUserId(userId);
        userClub.setClubId(clubId);
        userClub.setJoinTime(LocalDateTime.now());
        return userClubMapper.insert(userClub) > 0;
    }

    @Override
    public boolean quitClub(Long userId, Long clubId) {
        return userClubMapper.deleteByUserIdAndClubId(userId, clubId) > 0;
    }

    @Override
    public List<Club> getJoinList(Long userId) {
        return clubMapper.selectJoinByUserId(userId);
    }

    @Override
    public boolean auditClub(Long clubId, Integer status, String reason) {
        Club club = new Club();
        club.setId(clubId);
        club.setStatus(status);
        club.setAuditReason(reason);
        return updateById(club);
    }

    @Override
    public List<Club> getAuditList() {
        return clubMapper.selectAuditList();
    }

    @Override
    public List<Club> getManagedClubs(Long userId) {
        return clubMapper.selectManagedClubs(userId);
    }

    @Override
    public boolean isClubAdmin(Long userId, Long clubId) {
        LambdaQueryWrapper<UserClub> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserClub::getUserId, userId)
                .eq(UserClub::getClubId, clubId)
                .eq(UserClub::getIsAdmin, 1);
        return userClubMapper.selectCount(wrapper) > 0;
    }
    @Override
    public boolean updateJoinStatus(Long clubId, Long userId, Integer status) {
        // 查询关联记录
        LambdaQueryWrapper<UserClub> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserClub::getUserId, userId)
                .eq(UserClub::getClubId, clubId);
        UserClub userClub = userClubMapper.selectOne(wrapper);
        if (userClub == null) {
            return false;
        }
        // 更新状态
        userClub.setStatus(status);
        // 如果通过审核且还未设置管理员标志，默认为普通成员（0）
        if (status == 1 && userClub.getIsAdmin() == null) {
            userClub.setIsAdmin(0);
        }
        return userClubMapper.updateById(userClub) > 0;
    }
    @Override
    public List<Club> getAuditListWithCreator() {
        return clubMapper.selectAuditListWithCreator();
    }
}