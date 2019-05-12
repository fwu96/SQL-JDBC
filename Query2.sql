with inf as(
    select store,max(unemploymentrate) as unemploymentrate ,max(fuelprice) as fuelprice
    from temporaldata
    group by store
)

select store
from inf
where unemploymentrate > 10 and fuelprice <= 4
order by store;
