SELECT 
    COUNT(*)
FROM (
    SELECT 
        COUNT(s.weekdate)
    FROM 
        Sales AS s
    INNER JOIN 
        Holidays AS h
        ON h.WeekDate = s.WeekDate
        AND h.IsHoliday = FALSE
    GROUP BY 
        h.weekdate
    HAVING    
        SUM(s.WeeklySales) > (
            SELECT 
                SUM(s.WeeklySales) / COUNT (DISTINCT h.weekdate)
            FROM
                sales AS s 
            INNER JOIN
                holidays AS h 
            ON s.weekdate = h.weekdate
            AND h.isholiday = TRUE 
    )
) AS tmp;
