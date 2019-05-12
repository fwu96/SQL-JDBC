WITH corr_info AS (
    SELECT 
    	c1, c2, c3, c4, neg, pos, zero
    FROM (
    	SELECT 
		CAST(CORR(t.temperature, s.weeklysales) AS FLOAT) AS c1,
    		CAST(CORR(t.fuelprice, s.weeklysales) AS FLOAT) AS c2,
		CAST(CORR(t.cpi, s.weeklysales) AS FLOAT) AS c3,
    		CAST(CORR(t.unemploymentrate, s.weeklysales) AS FLOAT) AS c4,
		CAST('-' AS CHAR(1)) AS neg,
		CAST('+' AS CHAR(1)) AS pos,
                CAST(' ' AS CHAR(1)) AS zero   	
   	FROM 
            temporaldata AS t
    	INNER JOIN sales AS s 
		ON t.store = s.store 
		AND t.weekdate = s.weekdate
	) AS tmp1
)
SELECT 
    CAST('Temperature' AS VARCHAR(20)) AS Attribute, 
	(CASE 
		WHEN c1 < 0 THEN neg
        WHEN c1 = 0 THEN zero
		ELSE pos
	END) AS Corr_Sign, 
	c1 AS Correlation FROM corr_info    
UNION ALL SELECT
    CAST('FuelPrice' AS VARCHAR(20)),
	(CASE
		WHEN c2 < 0 THEN neg
        WHEN c2 = 0 THEN zero
		ELSE pos
	END), 
	c2 FROM corr_info
UNION ALL SELECT
    CAST('CPI' AS VARCHAR(20)),
	(CASE
		WHEN c3 < 0 THEN neg
        WHEN c3 = 0 THEN zero
		ELSE pos
	END), 
	c3 FROM corr_info
UNION ALL SELECT
    CAST('UnemploymentRate' AS VARCHAR(20)),
	(CASE
		WHEN c4 < 0 THEN neg
        WHEN c4 = 0 THEN zero
		ELSE pos
	END), c4 FROM corr_info;
