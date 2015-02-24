BEGIN;

	CREATE OR REPLACE FUNCTION update_advice_num_conditions()
	RETURNS TRIGGER AS $$
	BEGIN
	  update advice set num_conditions = ( select count(*) from advice_condition where advice_condition.advice_id = advice.id);
	  return null;
	END; $$ LANGUAGE 'plpgsql';
	
	create trigger trg_update_advice_num_conditions AFTER INSERT OR UPDATE OR DELETE
	  ON advice_condition
	  EXECUTE PROCEDURE update_advice_num_conditions();
	
	-- Execute the trigger without changing the data itself
	update advice_condition SET id = id;  
END;