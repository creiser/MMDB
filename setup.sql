
CREATE OR REPLACE TYPE LuceneIndexer AS OBJECT
(
  key INTEGER,

  STATIC FUNCTION ODCIGetInterfaces(
    ifclist OUT sys.ODCIObjectList)
  RETURN NUMBER,

  STATIC FUNCTION ODCIIndexCreate (ia SYS.ODCIIndexInfo, parms VARCHAR2,
    env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCICreate(oracle.ODCI.ODCIIndexInfo, java.lang.String,
		oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexAlter (ia sys.ODCIIndexInfo, 
    parms IN OUT VARCHAR2, altopt number, env sys.ODCIEnv) RETURN NUMBER, 

  STATIC FUNCTION ODCIIndexDrop(ia SYS.ODCIIndexInfo, env SYS.ODCIEnv) 
    RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIDrop(oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIEnv)
		return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexExchangePartition(ia SYS.ODCIIndexInfo,
    ia1 SYS.ODCIIndexInfo, env SYS.ODCIEnv) RETURN NUMBER,

  STATIC FUNCTION ODCIIndexUpdPartMetadata(ia sys.ODCIIndexInfo, 
    palist sys.ODCIPartInfoList, env sys.ODCIEnv) RETURN NUMBER,

  STATIC FUNCTION ODCIIndexInsert(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    newval BLOB, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIInsert(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
		java.sql.Blob, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexDelete(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    oldval BLOB, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIDelete(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
		java.sql.Blob, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexUpdate(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    oldval BLOB, newval BLOB, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIUpdate(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
		java.sql.Blob, java.sql.Blob, oracle.ODCI.ODCIEnv) return 
		java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexStart(sctx IN OUT LuceneIndexer, ia SYS.ODCIIndexInfo,
    op SYS.ODCIPredInfo, qi sys.ODCIQueryInfo, strt number, stop number,
    cmpval BLOB, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIStart(oracle.sql.STRUCT[], oracle.ODCI.ODCIIndexInfo, 
		oracle.ODCI.ODCIPredInfo, 
		oracle.ODCI.ODCIQueryInfo, java.math.BigDecimal, 
		java.math.BigDecimal, 
                java.sql.Blob, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  MEMBER FUNCTION ODCIIndexFetch(nrows NUMBER, rids OUT SYS.ODCIridlist,
    env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIFetch(java.math.BigDecimal, 
		oracle.ODCI.ODCIRidList[], oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  MEMBER FUNCTION ODCIIndexClose(env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'LuceneIndexer.ODCIClose(oracle.ODCI.ODCIEnv) return java.math.BigDecimal'
);
/

CREATE OR REPLACE TYPE BODY LuceneIndexer AS 
  STATIC FUNCTION ODCIGetInterfaces(ifclist OUT sys.ODCIObjectList) 
  RETURN NUMBER IS
  BEGIN
    ifclist := sys.ODCIObjectList(sys.ODCIObject('SYS','ODCIINDEX2'));
    return ODCIConst.Success;
  END ODCIGetInterfaces;

  STATIC FUNCTION ODCIIndexAlter (ia sys.ODCIIndexInfo, 
    parms IN OUT VARCHAR2, altopt number, env sys.ODCIEnv)
  RETURN NUMBER IS
  BEGIN
   return ODCIConst.Success;
  END ODCIIndexAlter;

  STATIC FUNCTION ODCIIndexExchangePartition(ia SYS.ODCIIndexInfo,
    ia1 SYS.ODCIIndexInfo, env SYS.ODCIEnv)
  RETURN NUMBER IS
  BEGIN
   return ODCIConst.Success;
  END ODCIIndexExchangePartition;

  STATIC FUNCTION ODCIIndexUpdPartMetadata(ia sys.ODCIIndexInfo, 
    palist sys.ODCIPartInfoList, env sys.ODCIEnv)
  RETURN NUMBER IS
  BEGIN
   return ODCIConst.Success;
  END ODCIIndexUpdPartMetadata;
END;
/

CREATE OR REPLACE FUNCTION bt_is_lucene_similar(a BLOB, b BLOB) RETURN NUMBER AS
BEGIN 
  RETURN 1;
END;
/

CREATE OR REPLACE OPERATOR is_lucene_similar 
BINDING (BLOB, BLOB) RETURN NUMBER 
USING bt_is_lucene_similar;

CREATE OR REPLACE INDEXTYPE LuceneIndex
FOR
is_lucene_similar (BLOB, BLOB)
USING LuceneIndexer;

CREATE TABLE Images (image BLOB);


