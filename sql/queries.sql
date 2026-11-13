use lapka_db;

-- 1. собаки, які шукають дім, у Києві
select p.pet_id, p.name, p.breed, p.age_months, s.name as shelter, s.city
from pets p
join shelters s on s.shelter_id = p.shelter_id
where p.species = 'dog' and p.status = 'available' and s.city = 'Київ';

-- 2. картка тварини з id = 1 (Барон)
select p.name, p.species, p.breed, p.age_months, p.status, p.description,
       s.name as shelter, s.city, s.phone
from pets p
join shelters s on s.shelter_id = p.shelter_id
where p.pet_id = 1;

-- 2. медична книжка тварини з id = 1
select procedure_name, record_date, notes
from medical_records
where pet_id = 1
order by record_date desc;

-- 3. статистика по притулках
select s.name, s.city,
       count(p.pet_id) as total,
       count(case when p.status = 'available' then 1 end) as available,
       count(case when p.status = 'reserved' then 1 end) as reserved,
       count(case when p.status = 'adopted' then 1 end) as adopted,
       count(case when p.status = 'archived' then 1 end) as archived,
       (select count(*)
        from adoption_applications a
        join pets p2 on p2.pet_id = a.pet_id
        where p2.shelter_id = s.shelter_id and a.status = 'new') as new_applications
from shelters s
left join pets p on p.shelter_id = s.shelter_id
group by s.shelter_id, s.name, s.city
order by total desc;

-- 4. тварини, у яких остання вакцинація була більше 6 місяців тому або її не було
select p.pet_id, p.name, s.name as shelter, max(m.record_date) as last_vaccination
from pets p
join shelters s on s.shelter_id = p.shelter_id
left join medical_records m on m.pet_id = p.pet_id and m.record_type = 'vaccination'
where p.status in ('available', 'reserved')
group by p.pet_id, p.name, s.name
having last_vaccination is null
    or last_vaccination < date_sub(curdate(), interval 6 month)
order by last_vaccination;

-- 5. подати анкету на усиновлення (перевірка з rollback)
start transaction;

select name, status from pets where pet_id = 3 for update;

insert into adoption_applications (pet_id, full_name, phone, reason)
values (3, 'Тестова Людина', '+380501234567', 'Перевірка запиту');

update pets set status = 'reserved' where pet_id = 3 and status = 'available';

select p.name, p.status, a.full_name, a.status as app_status
from pets p
join adoption_applications a on a.pet_id = p.pet_id
where p.pet_id = 3;

rollback;

select name, status from pets where pet_id = 3;

-- 6. схвалити анкету (перевірка з rollback)
start transaction;

select a.pet_id, a.status as app_status, p.status as pet_status
from adoption_applications a
join pets p on p.pet_id = a.pet_id
where a.application_id = 2
for update;

update adoption_applications set status = 'approved' where application_id = 2;

update pets set status = 'adopted', adopted_at = curdate() where pet_id = 11;

update adoption_applications set status = 'rejected'
where pet_id = 11 and status = 'new' and application_id <> 2;

select application_id, full_name, status from adoption_applications where pet_id = 11;
select name, status, adopted_at from pets where pet_id = 11;

rollback;

select name, status, adopted_at from pets where pet_id = 11;