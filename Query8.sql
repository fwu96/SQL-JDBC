with storedeptallsale as(
    select store,dept,sum(weeklysales) as all_sale
    from sales
    group by store,dept
),
inf as(
    select dept,sum(all_sale/size) as normsales
    from storedeptallsale
    join stores on storedeptallsale.store = stores.store
    group by dept
)

select dept,normsales
from inf
order by normsales desc
limit 10;