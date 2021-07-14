package com.example.demo.controller;

import com.example.demo.DateUtil;
import com.example.demo.FileUploadUtil;
import com.example.demo.model.Blog;
import com.example.demo.model.User;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import com.github.rjeschke.txtmark.Processor;
import org.attoparser.dom.IDOMMarkupParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BlogController {
    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    private static List<String> categories = new ArrayList<String>();

    static{
        categories.add("Food");
        categories.add("Travel");
        categories.add("IT");
        categories.add("Health");
        categories.add("Gaming");
    }

    @GetMapping("/login")
    public String viewLoginPage(Model model){
        model.addAttribute(new User());
        return "login";
    }

    @GetMapping("/signup")
    public String viewSignupPage(Model model){
        model.addAttribute(new User());
        return "signup";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("user_id");
        session.removeAttribute("username");
        session.invalidate();
        return "logout";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,HttpServletRequest request,Model model){
        HttpSession session = request.getSession();
        Optional<User> optUser = userRepository.findByUsername(user.getUsername());
        String message = null;
        if(optUser.isPresent()){
            message = "Username already exists!";
        }else{
            if(user.getPassword().length() >= 8) {
               user.setActive(true);
               user.setRole("ROLE_USER");
               userRepository.save(user);
               return "redirect:/login";
            }else{
                message="Password's length must be greater than or equal to 8";
            }
        }
        model.addAttribute("message", message);
        return "/signup";
    }

    @PostMapping("/verify")
    public String verfiyUser(@ModelAttribute User user,HttpServletRequest request,Model model){
        HttpSession session = request.getSession();
        Optional<User> optUser = userRepository.findByUsername(user.getUsername());
        String message = null;
        if(optUser.isPresent()){
            if(optUser.get().getPassword().equals(user.getPassword())){
                session.setAttribute("user_id",optUser.get().getId());
                session.setAttribute("username",optUser.get().getUsername());
                return "redirect:/blogs";
            }else{
                message = "Invalid Credentials!";
            }
        }else{
            message = "Username does not exist!";
        }
        model.addAttribute("message", message);
        return "/login";
    }

    @GetMapping("/blogs")
    public String getAllBlogs(Model model, HttpSession session){
            Long currUser = (Long)session.getAttribute("user_id");
            if(currUser == null){
                return "redirect:/login";
            }
            List<Blog> blogs = blogRepository.findAllByOrderByDateTimeDesc();
            List<Blog> categoryBlogs = blogRepository.findAllByCategoryOrderByDateTimeDesc(categories.get(0));
            model.addAttribute("article",blogs.remove(0));
            model.addAttribute("blogs",blogs);
            model.addAttribute("categoryBlogs",categoryBlogs);
            model.addAttribute("categories",categories);
            model.addAttribute("currUser",(String)session.getAttribute("username"));
            model.addAttribute("dateUtil",new DateUtil());
            if(session.getAttribute("message") != null){
                model.addAttribute("message",(String)session.getAttribute("message"));
            }
            session.setAttribute("message",null);
//            model.addAttribute("imgUtil",new ImageUtil());
            return "home";
    }


    @GetMapping("/blog/add")
    public String viewAddBlogPage(Model model,HttpSession session){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Blog blog = new Blog();
        model.addAttribute("blog",blog);
        model.addAttribute("categories",categories);
        model.addAttribute("currUser",(String)session.getAttribute("username"));
        return "addBlog";
    }

    @PostMapping("/blog/add")
    public String addBlog(Model model,@ModelAttribute Blog blog,HttpSession session, @RequestParam("file") MultipartFile multipartFile) throws IOException {
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Optional<User> user = userRepository.findById(currUser);
        Date date = new Date();
        blog.setDateTime(date);
        blog.setLikes(0);
        blog.setUser(user.get());

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        blog.setPic(fileName);
        Blog savedBlog = blogRepository.save(blog);
        String uploadDir = "user-photos/" + savedBlog.getId();
        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        session.setAttribute("message","Blog added Successfully!");
        return "redirect:/blogs";
    }

    @GetMapping("/blog/like/{id}")
    public String likeBlog(@PathVariable Long id,HttpSession session,Model model){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Optional<Blog> blog = blogRepository.findById(id);
        Blog resultBlog = blog.get();
        Set<User> users = resultBlog.getUsers();
        Optional<User> optionalUser = userRepository.findById(currUser);
        if(!users.contains(optionalUser.get())){
            users.add(optionalUser.get());
            Integer currLikes = resultBlog.getLikes();
            resultBlog.setLikes(currLikes + 1);
            blogRepository.save(resultBlog);
        }
        return "redirect:/blog/{id}";
    }

    @GetMapping("/blog/delete/{id}")
    public String deleteBlog(@PathVariable Long id,HttpSession session) throws IOException {
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Blog blog = blogRepository.findDistinctById(id);

        String currFile = blog.getPic();
        String uploadDir = "user-photos/" + blog.getId();
        FileUploadUtil.deleteFile(uploadDir, currFile);

        blog.setUsers(null);
        blog.setUser(null);

        blogRepository.save(blog);
        blogRepository.delete(blog);

        session.setAttribute("message","Blog deleted Successfully!");
        return "redirect:/blogs";
    }

    @GetMapping("/blog/{id}")
    public String viewBlogPage(@PathVariable Long id,Model model,HttpSession session) throws UnsupportedEncodingException {
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Optional<Blog> optBlog = blogRepository.findById(id);
        Blog blog = optBlog.get();
        model.addAttribute("blog",blog);
        model.addAttribute("blogDescription",Processor.process(blog.getDescription()));
        model.addAttribute("currUser",currUser);
        model.addAttribute("currUser",(String)session.getAttribute("username"));
        model.addAttribute("dateUtil",new DateUtil());
        return "blog";
    }

    @GetMapping("/blog/edit/{id}")
    public String viewEditBlogPage(@PathVariable Long id,Model model,HttpSession session){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Optional <Blog> blog = blogRepository.findById(id);
        model.addAttribute("blog",blog.get());
        model.addAttribute("categories",categories);
        model.addAttribute("currUser",currUser);
        return "editBlog";
    }

    @PostMapping("/blog/edit/{id}")
    public String editBlog(@PathVariable Long id, @ModelAttribute Blog blog,HttpSession session,@RequestParam("file") MultipartFile multipartFile ) throws IOException{
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        Optional<Blog> optionalBlog = blogRepository.findById(id);
        Blog toBeEditedBlog = optionalBlog.get();
        toBeEditedBlog.setTitle(blog.getTitle());
        toBeEditedBlog.setDescription(blog.getDescription());
        toBeEditedBlog.setAuthor(blog.getAuthor());
        toBeEditedBlog.setDateTime(new Date());
        toBeEditedBlog.setCategory(blog.getCategory());
        toBeEditedBlog.setUser(userRepository.findById(currUser).get());

        String currFile = toBeEditedBlog.getPic();
        String uploadDir = "user-photos/" + toBeEditedBlog.getId();
        FileUploadUtil.deleteFile(uploadDir,currFile);

        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        toBeEditedBlog.setPic(fileName);
        Blog savedBlog = blogRepository.save(toBeEditedBlog);
        String dir = "user-photos/" + savedBlog.getId();
        FileUploadUtil.saveFile(dir, fileName, multipartFile);

        session.setAttribute("message","Blog updated Successfully!");
        return "redirect:/blogs";
    }

    @GetMapping("/blog/manage")
    public String viewManageBlogPage(HttpSession session,Model model){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        List<Blog> blogs = blogRepository.findByUserIdOrderByDateTimeDesc(currUser);
        Set<String> categories = new HashSet<>();
        blogs.stream()
                .forEach(blog -> categories.add(blog.getCategory()));
        model.addAttribute("categories",categories);
        model.addAttribute("blogs",blogs);
        model.addAttribute("currUser",(String)session.getAttribute("username"));
        return "manageBlogs";
    }

    @GetMapping("/blog/manage/category/{category}")
    public String getCategoryWiseBlogsOfUser(@PathVariable String category,HttpSession session,Model model){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        List<Blog> blogs = blogRepository.findByUserIdOrderByDateTimeDesc(currUser);
        Set<String> categories = new HashSet<>();
        blogs.stream()
                .forEach(blog -> categories.add(blog.getCategory()));
        List<Blog> filteredBlogs = blogs.stream()
                .filter(blog -> blog.getCategory().equals(category))
                .collect(Collectors.toList());
        model.addAttribute("blogs",filteredBlogs);
        model.addAttribute("categories",categories);
        model.addAttribute("currUser",(String)session.getAttribute("username"));
        model.addAttribute("dateUtil",new DateUtil());
        return "manageBlogs";
    }


    @GetMapping("/blog/category/{category}")
    public String getCategoryWiseBlogs(@PathVariable String category,HttpSession session,Model model){
        Long currUser = (Long)session.getAttribute("user_id");
        if(currUser == null){
            return "redirect:/login";
        }
        List<Blog> categoryBlogs = blogRepository.findAllByCategoryOrderByDateTimeDesc(category);
        List<Blog> blogs = blogRepository.findAllByOrderByDateTimeDesc();
        model.addAttribute("article",blogs.remove(0));
        model.addAttribute("blogs",blogs);
        model.addAttribute("categoryBlogs",categoryBlogs);
        model.addAttribute("categories",categories);
        model.addAttribute("currUser",(String)session.getAttribute("username"));
        model.addAttribute("dateUtil",new DateUtil());
        return "home";
    }

}
