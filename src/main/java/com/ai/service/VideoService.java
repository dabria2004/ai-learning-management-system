package com.ai.service;

import com.ai.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public int findVideoCount(int moduleId) {
        return videoRepository.countByModuleId(moduleId);
    }
}
