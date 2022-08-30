package com.ai.controller;

import com.ai.entity.Course;
import com.ai.entity.Module;
import com.ai.entity.User;
import com.ai.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private FileService fileService;

    @GetMapping("home")
    public String home(){
        return "ADM-DB001";
    }

    @GetMapping("course-list")
    public String courseList(){
        return "ADM-CT001";
    }

    @PostMapping("course-create")
    public String postCreate(@Validated @ModelAttribute Course course, BindingResult bs, RedirectAttributes attr, ModelMap m){
        if(bs.hasErrors()){
            return "ADM-CT001";
        }
        var c = courseService.findByName(course.getName());
        if(c != null) {
            attr.addFlashAttribute("error", "%s course has already existed!".formatted(course.getName()));
            return "redirect:/admin/course-list";
        }
        courseService.save(course);
        attr.addFlashAttribute("cmessage", "%s course created successfully!".formatted(course.getName()));
        return "redirect:/admin/course-list";
    }

    @GetMapping("course-detail")
    public String detail(ModelMap m, @RequestParam int courseId) {
        var modules = moduleService.findByCourseId(courseId);
        for(var module : modules) {
            var resourceCount = resourceService.findResourceCount(module.getId());
            var videoCount = videoService.findVideoCount(module.getId());
            module.setResourceCount(resourceCount);
            module.setVideoCount(videoCount);
        }
        m.put("modules", modules);
        m.put("courseName", courseService.findById(courseId).getName());
        return "ADM-MT001";
    }

    @PostMapping("/course-edit")
    public String editCourse(@RequestParam int id,
                             @RequestParam String name,
                             @RequestParam String description,
                             RedirectAttributes redirectAttrs) throws IllegalStateException, IOException {

        var course = courseService.findById(id);

        fileService.renameFolder(course.getName(), name);

        course.setName(name);
        course.setDescription(description);
        courseService.save(course);

        return "redirect:/admin/course-list";
    }

    @GetMapping("course-delete")
    public String deleteCourse(@RequestParam int courseId, @RequestParam String courseName)
            throws IOException {
        courseService.deleteById(courseId, courseName);
        return "redirect:/admin/course-list";
    }

    @PostMapping("module-create")
    public String moduleCreate(@Validated @ModelAttribute Module module, BindingResult bs,
                               @RequestParam int courseId,
                               RedirectAttributes attr, ModelMap map) throws IllegalStateException, IOException {

        if(bs.hasErrors()) {
            return "ADM-MT001";
        }
        var m = moduleService.findByName(module.getName());
        if(m != null) {
            attr.addFlashAttribute("error", "%s module has already existed!".formatted(module.getName()));
            return "redirect:/admin/course-detail?courseId=%d".formatted(courseId);
        }
        module.setCourse(courseService.findById(courseId));
        moduleService.save(module);

        attr.addFlashAttribute("message", "%s module created successfully!".formatted(module.getName()));
        return "redirect:/admin/course-detail?courseId=%d".formatted(courseId);
    }

    @GetMapping("resource-list")
    public String resourceList(@RequestParam int moduleId, ModelMap m){
        var resources = resourceService.findByModuleId(moduleId);
        m.put("resources", resources);
        var module = moduleService.findById(moduleId);
        m.put("moduleName", module.getName());
        m.put("course", module.getCourse());
        return "ADM-RS001";
    }

    @PostMapping("module-edit")
    public String moduleEdit(@RequestParam int moduleId, @RequestParam String name, @RequestParam int courseId) throws IOException {
        var m = moduleService.findById(moduleId);
        var courseName = courseService.findById(courseId).getName();
        fileService.renameFolder(courseName.concat("\\").concat(m.getName()), name);
        m.setName(name);
        m.setVideo(new MultipartFile[]{});
        m.setResource(new MultipartFile[]{});
        moduleService.edit(m);
        return "redirect:/admin/course-detail?courseId=%d".formatted(courseId);
    }

    @GetMapping("module-delete")
    public String deleteCourse(@RequestParam int moduleId, RedirectAttributes attr)
            throws IOException {
        var m = moduleService.findById(moduleId);
        var courseId = m.getCourse().getId();
        moduleService.deleteById(moduleId, m.getName(), courseId);
        attr.addFlashAttribute("message", "%s module deleted successfully!".formatted(m.getName()));
        return "redirect:/admin/course-detail?courseId=%d".formatted(courseId);
    }

    @GetMapping("resource-delete")
    public String deleteResource(@RequestParam int resourceId, RedirectAttributes attributes){
        var resource = resourceService.findById(resourceId);
        var moduleName = resource.getModule().getName();
        var courseName = resource.getModule().getCourse().getName();
        resourceService.deleteById(resource, moduleName, courseName);
        attributes.addFlashAttribute("message", "%s resource deleted successfully!".formatted(resource.getName()));
        return "redirect:/admin/resource-list?moduleId=%d".formatted(resource.getModule().getId());
    }

    @ModelAttribute("courses")
    public List<Course> courses() {
        return courseService.findAll();
    }

    @ModelAttribute("course")
    public Course course() {
        return new Course();
    }

    @ModelAttribute("module")
    Module module() {
        return new Module();
    }


    @ModelAttribute("teacher")
    public User user() {
        return new User();
    }

    @PostMapping("teacher-register")
    public String postCreate(@Validated @ModelAttribute User user, BindingResult bs, RedirectAttributes attr, ModelMap m){
        if(bs.hasErrors()){
            return "ADM-TC001";
        }
        var c = userService.findByLoginId(user.getLoginId());
        if(c != null) {
            attr.addFlashAttribute("error", "%s course has already existed!".formatted(user.getLoginId()));
            return "redirect:/admin/course-list";
        }
        userService.save(user);
        attr.addFlashAttribute("cmessage", "%s course created successfully!".formatted(user.getName()));
        return "redirect:/admin/teacher-list";
    }

    @GetMapping("teacher-list")
    public String teacherList(ModelMap m) {
        m.put("teachers", userService.findUserByTeacherRole());
        return "ADM-TC001";
    }
}
