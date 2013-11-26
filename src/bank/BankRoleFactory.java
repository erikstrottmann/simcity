package bank;

import gui.Building;

import java.util.HashMap;
import java.util.Map;

import agent.Role;
import agent.RoleFactory;
import agent.interfaces.Person;



public class BankRoleFactory implements RoleFactory{

	Map<Person, Role> existingRoles;// = new HashMap<Person, bank.BankCustomerRole>();
	private Building bank;
	
	public BankRoleFactory(Building bank) {
		this.bank = bank;
		this.existingRoles = new HashMap<Person, Role>();
	}
	@Override
	public Role getCustomerRole(Person person) {
		Role role = existingRoles.get(person);
		if(role == null) {
			role = new BankCustomerRole(person, bank);
			role.setLocation(bank);
		}
		else {
			role.setPerson(person);
		}
		person.addRole(role);
		return role;
	}
}
