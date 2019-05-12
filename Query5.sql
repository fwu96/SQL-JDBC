--valid one
--select distinct s1.store
--from hw2.sales s1
--where s1.store not in (select distinct s2.store
--			from hw2.sales s2
--			where s2.weeklysales = 0 and extract(year from weekdate) = 2010
--			or extract(year from weekdate) = 2011 or extract(year from weekdate) = 2012)


with month_sum as (select distinct store, dept, extract(year from weekdate) as year, extract(month from weekdate) as month, sum(weeklysales) as monthSum
		   from hw2.sales
		   group by dept, store, extract(year from weekdate), extract(month from weekdate)
		   order by extract(year from weekdate), extract(month from weekdate))
select distinct store
from hw2.sales
where store not in (
	select distinct store
	from month_sum
	where monthSum <= 0 or monthSum = null and year = 2010
	intersect
	select distinct store
	from month_sum
	where monthSum <= 0 or monthSum = null and year = 2011 
	intersect
	select distinct store
	from month_sum
	where monthSum <= 0 or monthSum = null and year = 2012
)
