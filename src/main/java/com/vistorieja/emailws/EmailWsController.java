package com.vistorieja.emailws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @RequestMapping(path = "/email-send/{email}/{user}/{pass}", method = RequestMethod.GET)
    public HttpStatus sendMail(@PathVariable("email") String email, @PathVariable("user") String user, @PathVariable("pass") String pass) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmail(user, message, pass);

        try {
            mailSender.send(message);
            return HttpStatus.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return HttpStatus.NOT_FOUND;
        }
    }

    private void montarEmail(String usuario, SimpleMailMessage email, String newPassword) {

        email.setSubject("[VistorieJá] - Recuperação de Senha");
        email.setFrom("contato@vistorieja.com");
        String corpoMsg =
                "\nBem-vindo, " + usuario + " \n" +
                        "Sua senha é: " + newPassword + "\n" +
                        "\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com \n"
                        + "http://www.vistorieja.com \n";
        email.setText(corpoMsg);
    }
}
