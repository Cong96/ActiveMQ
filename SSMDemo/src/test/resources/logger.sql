
---Oracle ĿǰVARCHAR��VARCHAR2��ͬ��ʡ���ҵ��׼��VARCHAR���Ϳ��Դ洢���ַ���������oracle���������������������Ժ���������Ȩ����Oracle�Լ�������һ����������VARCHAR2��������Ͳ���һ����׼��VARCHAR�����������ݿ���varchar�п��Դ洢���ַ��������Ը�Ϊ�洢NULLֵ����������������ݵ�������Oracle����ʹ��VARCHAR2������VARCHAR��

-----mysql
create table sys_log(
	log_id varchar(50),
	description varchar(100),
	method varchar(20),
	logtype varchar(50),
	requestip varchar(50),
	execption_code varchar(30),
	exception_detail text,
	params varchar(100),
	create_user varchar(30),

	create_date datetime
);



----datetime Mysql��ʹ�ã�Oracle��ֻ��Date�������ͣ�
---�����Դ洢�£��꣬�գ����ͣ�ʱ���ֺ��롣�����͵�������ʾʲôʱ�������Ѿ�������Ҫ������
-----Date�������;�ȷ����