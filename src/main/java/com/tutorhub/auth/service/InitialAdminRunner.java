package com.tutorhub.auth.service;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
@Component class InitialAdminRunner implements ApplicationRunner { private final AuthService auth; InitialAdminRunner(AuthService auth) { this.auth = auth; } public void run(ApplicationArguments args) { auth.createInitialAdminIfConfigured(); } }
