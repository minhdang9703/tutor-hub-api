package com.tutorhub.auth.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service class SmtpEmailSender implements EmailSender { private final JavaMailSender sender; SmtpEmailSender(JavaMailSender sender) { this.sender = sender; } public void send(String recipient, String subject, String body) { var message = new SimpleMailMessage(); message.setTo(recipient); message.setSubject(subject); message.setText(body); sender.send(message); } }
