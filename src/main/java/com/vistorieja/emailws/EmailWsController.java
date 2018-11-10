package com.vistorieja.emailws;

import com.vistorieja.emailws.util.CriptoUtil;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
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
import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmailWsController {

    public static final String EMAIL_CONTATO = "contato@vistorieja.com";
    public static final String VISTORIE_JÁ_BEM_VINDO = "[VistorieJá] - Bem vindo";
    public static final String VISTORIE_JA_ALTERACAO = "[VistorieJá] - Esqueci a senha";
    public static final String VISTORIE_JA_CONFIRMACAO = "[VistorieJá] - Confirmação de cadastro";
    public static final String TEMPLATE_CONFIRMACAO = "email_confirmacao.vm";
    public static final String TEMPLATE_BOAS_VINDAS = "email_boas_vindas.vm";
    public static final String TEMPLATE_ALTERACAO_SENHA = "email_alteracao_senha.vm";

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private JavaMailSender javaMailSender;


    @RequestMapping(path = "/email-send/confirmation/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendMimeEmail(@PathVariable("email") String email, @PathVariable("user") String usuario){
        MimeMessagePreparator prep = null;
        return montarEnvioEmail(email, usuario, VISTORIE_JÁ_BEM_VINDO, TEMPLATE_BOAS_VINDAS);
    }


    @RequestMapping(path = "/email-send/{email}/{user}", method = RequestMethod.GET)
    public HttpStatus sendMail(@PathVariable("email") String email, @PathVariable("user") String usuario) {
        MimeMessagePreparator prep = null;
        return montarEnvioEmail(email, usuario, VISTORIE_JA_ALTERACAO, TEMPLATE_ALTERACAO_SENHA);
    }


    @RequestMapping(path = "/email-send/signup-sucess/{email}/{user}", method = RequestMethod.GET)
    public void sendEmailConfirmationSignup(@PathVariable("email") String email, @PathVariable("user") String usuario) throws MessagingException {
        MimeMessagePreparator prep = criarEmailTemplate(email, usuario,VISTORIE_JA_CONFIRMACAO, TEMPLATE_CONFIRMACAO);
        javaMailSender.send(prep);
    }


    private HttpStatus montarEnvioEmail(String email, String usuario, String vistorieJáBemVindo, String templateBoasVindas) {
        MimeMessagePreparator prep;
        try {
            prep = criarEmailTemplate(email, usuario, vistorieJáBemVindo, templateBoasVindas);
            javaMailSender.send(prep);
            return HttpStatus.OK;
        } catch (MessagingException e) {
            return HttpStatus.NOT_FOUND;
        }
    }


    private MimeMessagePreparator criarEmailTemplate(String email,String usuario, String assunto, String template) throws MessagingException {
        return new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(
                        mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED,
                        "UTF-8");
                message.setTo(email);
                message.setFrom(EMAIL_CONTATO);
                message.setSubject(assunto);
                ClassLoader classLoader = Thread.currentThread()
                        .getContextClassLoader();
                if (classLoader == null) {
                    classLoader = this.getClass().getClassLoader();
                }

                Map<String, Object> model = new HashMap<String, Object>();
                model.put("usuario", usuario);

                switch (template){
                    case TEMPLATE_CONFIRMACAO:
                        model.put("link", "http://www.vistorieja.com.br/rest/usuario/confirmation/" + CriptoUtil.encrypt(email));
                        break;
                    case TEMPLATE_BOAS_VINDAS:
                        break;
                    case TEMPLATE_ALTERACAO_SENHA:
                        model.put("link", "http://www.vistorieja.com.br/updatePassword?key="+ CriptoUtil.encrypt(email));
                        break;
                }


                String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, "UTF-8", model);

                // --Create the HTML body part of the message
                MimeBodyPart mimeBody = new MimeBodyPart();
                mimeBody.setContent(text, "text/html");
                MimeBodyPart mimeImage = new MimeBodyPart();
                DataSource ds = new URLDataSource(classLoader.getResource("images/header_email.png"));
                mimeImage.setDataHandler(new DataHandler(ds));
                mimeImage.setFileName("header_email.png");
                mimeImage.setHeader("Content-ID", "<image>");

                Multipart multipart = new MimeMultipart("relative");
                multipart.addBodyPart(mimeBody);
                multipart.addBodyPart(mimeImage);
                mimeMessage.setContent(multipart);
            }
        };
    }
}
