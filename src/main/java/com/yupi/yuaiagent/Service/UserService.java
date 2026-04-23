package com.yupi.yuaiagent.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yupi.yuaiagent.config.JwtUtil;
import com.yupi.yuaiagent.entity.User;
import com.yupi.yuaiagent.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 注册
    public void register(RegisterRequest request) throws Exception {
        // 检查邮箱是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new Exception("邮箱已被注册");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
    }

    // 登录
    public LoginResponse login(LoginRequest request) throws Exception {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new Exception("邮箱或密码错误");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        return response;
    }
}