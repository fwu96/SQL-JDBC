--valid
drop table if exists q10Report;

create table q10Report as

with type_sum as(
    select 
          sum(sa1.weeklysales) as preSum, 
	  extract(year from weekDate) as year, 
	  (cast(extract(month from weekDate) as integer) + 2) / 3 as quarter, 
	  st1.type
    from hw2.sales sa1, 
         hw2.stores st1
    where sa1.store = st1.store
    group by extract(year from weekDate), 
             (cast(extract(month from weekDate) as integer) + 2) / 3, 
	     st1.type
    order by st1.type, extract(year from weekDate), (cast(extract(month from weekDate) as integer) + 2) / 3
)


             select
                   tempA.year as yr, tempA.quarter as qtr, tempA.preSum as store_a_sales, tempB.preSum as store_b_sales, tempC.preSum as store_c_sales
             from (select preSum, year, quarter from type_sum where type = 'A') as tempA,
                  (select preSum, year, quarter from type_sum where type = 'B') as tempB,
                  (select preSum, year, quarter from type_sum where type = 'C') as tempC,
                  type_sum
             where tempA.year = tempB.year and tempB.year = tempC.year and tempA.quarter = tempB.quarter and tempB.quarter = tempC.quarter
             group by tempA.year, tempA.quarter, tempA.preSum, tempB.preSum, tempC.preSum
             order by tempA.year, tempA.quarter;


insert into q10Report(yr, qtr, store_a_sales, store_b_sales, store_c_sales)
select yr, null, sum(store_a_sales), sum(store_b_sales), sum(store_c_sales)
from q10Report
group by yr;

select * from q10Report order by yr, qtr;
