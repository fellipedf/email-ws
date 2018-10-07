package com.vistorieja.emailws;

import com.vistorieja.emailws.util.CriptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class EmailWsController {

    @Autowired
    private MailSender mailSender;

    @RequestMapping(path = "/email-send/{email}/{user}/{pass}", method = RequestMethod.GET)
    public HttpStatus sendMail(@PathVariable("email") String email, @PathVariable("user") String user, @PathVariable("pass") String pass) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmail(user, message, pass);

        return sendMail(message);
    }

    private HttpStatus sendMail(SimpleMailMessage message) {
        try {
            mailSender.send(message);
            return HttpStatus.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return HttpStatus.NOT_FOUND;
        }
    }

    @RequestMapping(path = "/email-send/confirmation/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendMail(@PathVariable("email") String email, @PathVariable("user") String user){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmailConfirmacao(user, message,email);

        return sendMail(message);
    }

    @RequestMapping(path = "/email-send/change-password/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendMailChangePassword(@PathVariable("email") String email, @PathVariable("user") String user){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmailMudarSenha(message,email,user);

        return sendMail(message);
    }

    @RequestMapping(path = "/email-send/change-password-sucess/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendMailConfirmationPasswordChanged(@PathVariable("email") String email, @PathVariable("user") String user){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmailConfirmacao(user, message,email);

        return sendMail(message);
    }




    private void montarEmailMudarSenha(SimpleMailMessage message, String email, String user) {
        message.setSubject("[VistorieJá] - Confirme seu cadastro");
        message.setFrom("contato@vistorieja.com");
        String corpoMsg =
                "\nOlá, " + user + " \n" +
                        "Para alterar a senha, por favor clique no link abaixo:" +
                        "\n\n"
                        + "http://www.vistorieja.com.br/rest/esqueci/key=" + CriptoUtil.encrypt(email)
                        +"\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com.br \n"
                        + "http://www.vistorieja.com.br \n";
        message.setText(corpoMsg);
    }

    private void montarEmail(String usuario, SimpleMailMessage email, String newPassword) {

        email.setSubject("[VistorieJá] - Recuperação de Senha");
        email.setFrom("contato@vistorieja.com");
        String corpoMsg =
                "\nOlá, " + usuario + " \n" +
                        "Sua nova senha é: " + newPassword + "\n" +
                        "\n\n"+
                        "Para alterar a senha, por favor clique no link abaixo:" +
                        "\n\n"
                        + "http://www.vistorieja.com.br/esqueci/key=" + CriptoUtil.encrypt(usuario)
                        +"\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com.br \n"
                        + "http://www.vistorieja.com.br \n";
        email.setText(corpoMsg);
    }

    private void montarEmailConfirmacao(String usuario, SimpleMailMessage message, String email) {

        message.setSubject("[VistorieJá] - Confirme seu cadastro");
        message.setFrom("contato@vistorieja.com");
        String corpoMsg =
                "\nOlá, " + usuario + " \n" +
                        "Seja muito bem-vindo(a) ao VistorieJá" +
                        "\n\n"
                        + "Por favor confirme o seu email acessando o link abaixo:" +
                        "\n\n"
                        + "http://www.vistorieja.com.br/rest/confirmation/" + CriptoUtil.encrypt(usuario)
                        +"\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com.br \n"
                        + "http://www.vistorieja.com.br \n";
        message.setText(corpoMsg);
    }

    private void montarEmailConfirmacaoAlteracaoSenha(String usuario, SimpleMailMessage message, String email) {

        message.setSubject("[VistorieJá] - Confirme seu cadastro");
        message.setFrom("contato@vistorieja.com");
        String corpoMsg =
                "\nOlá, " + usuario + " \n" +
                        "Sua senha foi alterada com sucesso!"
                        +"\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com.br \n"
                        + "http://www.vistorieja.com.br \n";
        message.setText(corpoMsg);
    }

}
