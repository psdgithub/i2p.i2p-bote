/**
 * Copyright (C) 2009  HungryHobo@mail.i2p
 * 
 * The GPG fingerprint for HungryHobo@mail.i2p is:
 * 6DD3 EAA2 9990 29BC 4AD2 7486 1E2C 7B61 76DC DC12
 * 
 * This file is part of I2P-Bote.
 * I2P-Bote is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * I2P-Bote is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with I2P-Bote.  If not, see <http://www.gnu.org/licenses/>.
 */

package i2p.bote.web;

import static i2p.bote.Util._;
import i2p.bote.I2PBote;
import i2p.bote.Util;
import i2p.bote.email.Email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.Message.RecipientType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import net.i2p.util.Log;

public class SendEmailTag extends BodyTagSupport {
    private static final long serialVersionUID = 5746062176954959787L;
    
    private Log log = new Log(SendEmailTag.class);
    private String senderAddress;
    private List<Recipient> recipients = new ArrayList<Recipient>();
    private String subject;
    private String message;
    
    /**
     * Overridden to remove parameters from the previous SendEmailTag object so the old
     * parameters aren't used for the new SendEmailTag (at least Tomcat re-uses SendEmailTag objects)
     */
    @Override
    public int doStartTag() throws JspException {
        recipients.clear();
        return super.doStartTag();
    }
    
    @Override
    public int doEndTag() {
        JspWriter out = pageContext.getOut();

        Email email = new Email();
        String statusMessage;
        try {
            email.setSender(new InternetAddress(senderAddress));
            email.setSubject(subject, "UTF-8");
            email.setText(message, "UTF-8");
            for (Recipient recipient: recipients)
                email.addRecipient(recipient.type, recipient.address);
            email.fixAddresses();

            I2PBote.getInstance().sendEmail(email);
            statusMessage = _("The email has been queued for sending.");
        }
        catch (Exception e) {
            statusMessage = _("Error sending email: {0}", e.getLocalizedMessage());
            log.error("Error sending email", e);
        }

        try {
            out.println(statusMessage);
        } catch (IOException e) {
            log.error("Can't write output to HTML page", e);
        }
        return EVAL_PAGE;
    }

    /**
     * 
     * @param sender Can be a (Base64-encoded) email identity key or a public name plus
     * an email identity key.
     */
    public void setSender(String sender) {
        this.senderAddress = sender;
    }

    public String getSender() {
        return senderAddress;
    }

    void addRecipient(RecipientType type, Address address) {
        recipients.add(new Recipient(type, address));
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    private static class Recipient {
        RecipientType type;
        Address address;
        
        public Recipient(RecipientType type, Address address) {
            this.type = type;
            this.address = address;
        }
    }
}