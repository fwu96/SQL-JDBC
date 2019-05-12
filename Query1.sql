with week_sale as(
    select sales.store as store,sum(sales.weeklysales) as weeklysales
    from holidays
    join sales on holidays.weekdate = sales.weekdate
    where holidays.isholiday
    group by sales.store
)

(select store,weeklysales as hol_sales
from week_sale
order by weeklysales desc
limit 1)
union
(select store,weeklysales
from week_sale
order by weeklysales asc
limit 1);
