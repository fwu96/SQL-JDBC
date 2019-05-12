--valid one
select temp2.month, temp1.Type, temp2.monthSum, 100 * temp2.monthSum/temp1.totalSum as "%contribution"
from ( select sum(temp.storeSum) as totalSum, st1.Type
	from (select sum(sa1.weeklySales) as storeSum, sa1.store
		from hw2.Sales sa1
		group by sa1.store)as temp,
		hw2.Stores st1
	where st1.store = temp.store
	group by st1.Type) as temp1,
     ( select sum(weeklySales) as monthSum, hw2.stores.Type, extract(month from weekdate) as month
	from hw2.sales
	inner join hw2.stores on hw2.sales.store = hw2.stores.store
	group by extract(month from weekdate), hw2.stores.type
	) as temp2
where temp1.Type = temp2.Type
group by temp1.Type, temp2.month, temp2.monthSum, temp1.totalSum
order by temp1.type
