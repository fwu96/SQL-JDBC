with good_department as (
	select distinct temp3.dept
	from (select count (distinct temp1.store), temp1.dept
		from (select sum(weeklysales) as deptsale, dept, store
			from hw2.sales
			group by dept, store) as temp1,
		     (select sum(weeklysales) as storesale, store
			from hw2.sales
			group by store) as temp2
		where temp1.store = temp2.store
		      	and temp1.deptsale / temp2.storesale > 0.05
		group by temp1.dept) as temp3
	where temp3.count >= 3
)
select distinct temp6.dept, avg(temp6.contribution)
from (select temp4.dept, temp4.deptsum / temp5.storeSum as contribution, temp5.store
       from (select sum(s4.weeklysales) as deptSum, good_department.dept, s4.store
		from hw2.sales s4, good_department
		where s4.dept = good_department.dept
		group by s4.dept, s4.store, good_department.dept) as temp4,
            (select sum(s5.weeklysales) as storeSum, s5.store
	        from hw2.sales s5
	        group by s5.store) as temp5
      where temp4.store = temp5.store
      group by temp4.dept, temp4.deptSum / temp5.storeSum, temp5.store)as temp6
group by temp6.dept
