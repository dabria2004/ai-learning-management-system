package com.ai.service;

import com.ai.entity.Resource;
import com.ai.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository resourceRepository;

    public int findResourceCount(int moduleId) {
        return resourceRepository.countByModuleId(moduleId);
    }

    public List<Resource> findByModuleId(int moduleId) {
        return resourceRepository.findByModuleId(moduleId);
    }
}
