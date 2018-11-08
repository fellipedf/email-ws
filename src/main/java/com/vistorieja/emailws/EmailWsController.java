package com.vistorieja.emailws;

import com.vistorieja.emailws.util.CriptoUtil;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmailWsController {

    public static final String EMAIL_CONTATO = "contato@vistorieja.com";
    @Autowired
    private MailSender mailSender;
    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private JavaMailSender javaMailSender;


    @RequestMapping(path = "/email-send/template2/{email}/{user}", method = RequestMethod.GET)
    public void sendMimeEmail(@PathVariable("email") String email, @PathVariable("user") String usuario) {

    MimeMessagePreparator prep = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(
                        mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED,
                        "UTF-8");
                message.setTo(email);
                message.setFrom(EMAIL_CONTATO);
                message.setSubject("[VistorieJá] - Bem vindo");
                ClassLoader classLoader = Thread.currentThread()
                        .getContextClassLoader();
                if (classLoader == null) {
                    classLoader = this.getClass().getClassLoader();
                }

                Map<String, Object> model = new HashMap<String, Object>();
                model.put("usuario", usuario);
                model.put("link", "http://www.vistorieja.com.br/rest/usuario/confirmation/" + CriptoUtil.encrypt(email));
                String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "email.vm", "UTF-8", model);

                // --Create the HTML body part of the message
                MimeBodyPart mimeBody = new MimeBodyPart();
                mimeBody.setContent(text, "text/html");

                // --Create the image part of the message
                MimeBodyPart mimeImage = new MimeBodyPart();
                DataSource ds = new URLDataSource(
                        classLoader.getResource("images/logo_azul.png"));
                mimeImage.setDataHandler(new DataHandler(ds));
                mimeImage.setHeader("Content-ID", "logo");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBody);
                multipart.addBodyPart(mimeImage);
                mimeMessage.setContent(multipart);
            }
        };
        javaMailSender.send(prep);
    }

    @RequestMapping(path = "/email-send/template/{email}/{user}", method = RequestMethod.GET)
    public void  enviarEmailTemplate(@PathVariable("email") String email,@PathVariable("user") String usuario) throws MessagingException, IOException {


        Map<String, Object> model = new HashMap<String, Object>();
        model.put("usuario", usuario);
        model.put("link", "http://www.vistorieja.com.br/rest/usuario/confirmation/" + CriptoUtil.encrypt(email));
        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "email.vm", "UTF-8", model);


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(EMAIL_CONTATO);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("[VistorieJá] - Bem vindo");
        mimeMessageHelper.setText(text, true);
        System.out.println(text);
        javaMailSender.send(mimeMessage);

    }


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
        montarEmailConfirmacaoAlteracaoSenha(user, message,email);

        return sendMail(message);
    }

    @RequestMapping(path = "/email-send/signup-sucess/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendEmailConfirmationSignup(@PathVariable("email") String email, @PathVariable("user") String user){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        montarEmailConfirmacaoCadastro(user, message,email);

        return sendMail(message);
    }

    private void montarEmailMudarSenha(SimpleMailMessage message, String email, String user) {
        message.setSubject("[VistorieJá] - Confirme seu cadastro");
        message.setFrom(EMAIL_CONTATO);
        String corpoMsg =
                "\nOlá, " + user + " \n" +
                        "Para alterar a senha, por favor clique no link abaixo:" +
                        "\n\n"
                        + "http://www.vistorieja.com.br/esqueci"
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
                        + "http://www.vistorieja.com.br/updatePassword"
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
                        + "http://www.vistorieja.com.br/rest/usuario/confirmation/" + CriptoUtil.encrypt(email)
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
                        "Sua senha foi alterada com sucesso!"+
                        "\n\n"
                        + "Por favor confirme o seu email acessando o link abaixo:" +
                        "\n\n"+ "http://www.vistorieja.com.br/rest/usuario/confirmation/" + CriptoUtil.encrypt(email)
                        +"\n\n"
                        + "Atenciosamente,\n"
                        + "Formulário de Contato - VistorieJá \n\n"
                        + "E-mail: contato@vistorieja.com.br \n"
                        + "http://www.vistorieja.com.br \n";
        message.setText(corpoMsg);
    }

    private void montarEmailConfirmacaoCadastro(String usuario, SimpleMailMessage message, String email) {

        message.setSubject("[VistorieJá] - Boas vindas");
        message.setFrom("contato@vistorieja.com");
        String corpoMsg = " Olá, "+ usuario + "\n" +
                "Muito obrigado por se cadastrar no VistorieJá! " +
                "Agora você tem acesso a melhor plataforma de vistorias online.\n" +
                "Aqui nós garantimos um trabalho excelente, vamos começar?\n"
                + "Acesse o link: http://www.vistorieja.com.br\n"
                + "Atenciosamente,\n"
                + "Formulário de Contato - VistorieJá \n\n"
                + "E-mail: contato@vistorieja.com.br \n"
                + "http://www.vistorieja.com.br \n";
        message.setText(corpoMsg);
    }

}
