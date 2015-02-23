BEGIN;

	CREATE OR REPLACE FUNCTION update_reference_num_conditions()
	RETURNS TRIGGER AS $$
	BEGIN
	  update reference_value set num_conditions = ( select count(*) from reference_condition where reference_condition.reference_value_id = reference_value.id);
	  return null;
	END; $$ LANGUAGE 'plpgsql';
	
	create trigger trg_update_reference_num_conditions AFTER INSERT OR UPDATE OR DELETE
	  ON reference_condition
	  EXECUTE PROCEDURE update_reference_num_conditions();
	
	-- Execute the trigger without changing the data itself
	update reference_condition SET id = id;  
END;