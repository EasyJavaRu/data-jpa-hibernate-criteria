package ru.easyjava.data.jpa.hibernate.entity;

import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;

public class CriteriaTest {
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws Exception {
        Passport p = new Passport();
        p.setSeries("AS");
        p.setNo("123456");
        p.setIssueDate(LocalDate.now());
        p.setValidity(Period.ofYears(20));

        Address a = new Address();
        a.setCity("Kickapoo");
        a.setStreet("Main street");
        a.setBuilding("1");

        Person person = new Person();
        person.setFirstName("Test");
        person.setLastName("Testoff");
        person.setDob(LocalDate.now());
        person.setPrimaryAddress(a);
        person.setPassport(p);

        Company c = new Company();
        c.setName("Acme Ltd");

        p.setOwner(person);
        person.setWorkingPlaces(Collections.singletonList(c));

        entityManagerFactory = Persistence.createEntityManagerFactory("ru.easyjava.data.jpa.hibernate");
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.merge(person);
        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void testGreeter() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Person> personCriteria = cb.createQuery(Person.class);
        Root<Person> personRoot = personCriteria.from(Person.class);
        personCriteria.select(personRoot);
        em.createQuery(personCriteria)
                .getResultList()
                .forEach(System.out::println);

        CriteriaQuery<Passport> passportCriteria = cb.createQuery(Passport.class);
        Root<Person> personPassportRoot = passportCriteria.from(Person.class);
        passportCriteria.select(personPassportRoot.get("passport"));
        em.createQuery(passportCriteria)
                .getResultList()
                .forEach(System.out::println);

        CriteriaQuery<Passport> passportOwnerCriteria = cb.createQuery(Passport.class);
        Root<Passport> ownerPassportRoot = passportOwnerCriteria.from(Passport.class);
        passportOwnerCriteria.select(ownerPassportRoot);
        passportOwnerCriteria.where(cb.equal(ownerPassportRoot.get("owner").get("lastName"), "Testoff"));
        em.createQuery(passportOwnerCriteria)
                .getResultList()
                .forEach(System.out::println);

        CriteriaQuery<Passport> passportLikeCriteria = cb.createQuery(Passport.class);
        Root<Passport> likePassportRoot = passportLikeCriteria.from(Passport.class);
        passportLikeCriteria.select(likePassportRoot);
        passportLikeCriteria.where(cb.like(likePassportRoot.get("owner").get("lastName"), "Te%"));
        em.createQuery(passportLikeCriteria)
                .getResultList()
                .forEach(System.out::println);

        CriteriaQuery<Person> personWorkCriteria = cb.createQuery(Person.class);
        Root<Person> personWorkRoot = personWorkCriteria.from(Person.class);
        Join<Person, Company> company = personWorkRoot.join("workingPlaces");
        personWorkCriteria.select(personWorkRoot);
        personWorkCriteria.where(cb.equal(company.get("name"), "Acme Ltd"));
        em.createQuery(personWorkCriteria)
                .getResultList()
                .forEach(System.out::println);

        CriteriaQuery<Company> companyPassportCriteria = cb.createQuery(Company.class);
        Root<Company> companyPassportRoot = companyPassportCriteria.from(Company.class);
        Join<Company, Person> person = companyPassportRoot.join("workers");
        companyPassportCriteria.select(companyPassportRoot);
        companyPassportCriteria.where(cb.equal(person.get("passport").get("series"), "AS"));
        em.createQuery(companyPassportCriteria)
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList()
                .forEach(System.out::println);

        em.getTransaction().commit();
        em.close();
    }
}
