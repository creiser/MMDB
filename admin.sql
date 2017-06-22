-- Grant permission to the whole file system (SQL):
call dbms_java.grant_permission( 'PUBLIC', 'SYS:java.io.FilePermission', '<<ALL FILES>>', 'read,write,execute' );
/

-- For disabling the security manager (SQL):
call dbms_java.grant_permission( 'PUBLIC', 'SYS:java.lang.RuntimePermission', 'setSecurityManager', '' );
/

-- Grant priviliges to create indicies, etc. (execute one-by-one):

grant connect, resource to PUBLIC;
/
grant create operator to PUBLIC;
/
grant create indextype to PUBLIC;
/
grant create table to PUBLIC;
/
