package com.vistorieja.emailws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailWsController {

    @Autowired
    private MailSender mailSender;

    @RequestMapping(path = "/email-send/{email}/{msg}", method = RequestMethod.GET)
    public String sendMail(@PathVariable("email") String email, @PathVariable("msg") String msg) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setText(msg);
        message.setTo(email);
        message.setFrom("contato@vistorieja.com");
        message.setSubject("Hello Kibixinha");

        try {
            mailSender.send(message);
            return "Email enviado com sucesso!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao enviar email.";
        }
    }
}
