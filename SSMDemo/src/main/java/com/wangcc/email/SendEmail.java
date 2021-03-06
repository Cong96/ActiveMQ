package com.wangcc.email;

/**
 * @ClassName: SendEmail
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author wangcc
 * @date 2017年10月25日 下午2:35:14 一、RFC882文档简单说明
 *       RFC882文档规定了如何编写一封简单的邮件(纯文本邮件)，一封简单的邮件包含邮件头和邮件体两个部分，邮件头和邮件体之间使用空行分隔。
 * 
 *       邮件头包含的内容有：
 * 
 *       from字段 --用于指明发件人 to字段 --用于指明收件人 subject字段 --用于说明邮件主题 cc字段 --
 *       抄送，将邮件发送给收件人的同时抄送给另一个收件人，收件人可以看到邮件抄送给了谁 bcc字段 --
 *       密送，将邮件发送给收件人的同时将邮件秘密发送给另一个收件人，收件人无法看到邮件密送给了谁 邮件体指的就是邮件的具体内容。
 *       在我们的实际开发当中，一封邮件既可能包含图片，又可能包含有附件，在这样的情况下，RFC882文档规定的邮件格式就无法满足要求了。
 * 
 *       MIME协议是对RFC822文档的升级和补充，它描述了如何生产一封复杂的邮件。通常我们把MIME协议描述的邮件称之为MIME邮件。MIME协议描述的数据称之为MIME消息。
 *       对于一封复杂邮件，如果包含了多个不同的数据，MIME协议规定了要使用分隔线对多段数据进行分隔，并使用Content-Type头字段对数据的类型、以及多个数据之间的关系进行描述。
 * 
 */
public class SendEmail {

}
