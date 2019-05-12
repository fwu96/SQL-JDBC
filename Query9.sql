WITH storedeptallsale AS(
    SELECT store,dept,sum(weeklysales) AS all_sale
    FROM sales
    GROUP BY store,dept
), inf AS(
    SELECT dept, SUM(all_sale/SIZE) AS normsales
    FROM storedeptallsale
    JOIN stores ON storedeptallsale.store = stores.store
    GROUP BY dept
), deps AS (
SELECT dept AS d
FROM inf
ORDER BY normsales DESC
LIMIT 10
), info AS (
        SELECT dept, yr, mo, monthlysales,
                ROUND(CAST((monthlysales * 100 / total) AS DECIMAL), 2) AS contribution,
                ROUND(CAST(cumulative_sales AS DECIMAL), 2) AS cumulative_sales
        FROM (
                SELECT dept, mo, yr, monthlysales,
                        SUM(monthlysales) OVER (PARTITION BY dept ORDER BY dept, yr, mo) AS cumulative_sales,
                        SUM(monthlysales) OVER (PARTITION BY dept) AS total
                FROM (
                        SELECT SUM(weeklysales) AS monthlysales, dept,
                                EXTRACT(YEAR FROM weekdate) AS yr,
                                EXTRACT(MONTH FROM weekdate) AS mo
                        FROM sales
                        INNER JOIN deps ON sales.dept = deps.d
                        GROUP BY dept, yr, mo
                        ORDER BY yr, mo
                ) AS tmp
                ORDER BY dept
        ) AS tmp2
        ORDER BY dept, yr, mo
)
SELECT * FROM info
group by dept, mo, yr, monthlysales, contribution, cumulative_sales
ORDER BY dept, yr, mo;
