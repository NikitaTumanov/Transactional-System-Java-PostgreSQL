CREATE TABLE IF NOT EXISTS card
(
    id SERIAL PRIMARY KEY,
    num INTEGER NOT NULL UNIQUE,
    balance DECIMAL(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    debit_frozen_balance DECIMAL(18, 2) DEFAULT 0
);

INSERT INTO card (num, balance, currency_code, debit_frozen_balance)
VALUES (0000, 10000.00, 'USD', 0.00);
INSERT INTO card (num, balance, currency_code, debit_frozen_balance)
VALUES (0001, 71000.00, 'RUB', 0.00);
INSERT INTO card (num, balance, currency_code, debit_frozen_balance)
VALUES (0002, 700.00, 'UAH', 0.00);
INSERT INTO card (num, balance, currency_code, debit_frozen_balance)
VALUES (0003, 3200.00, 'EUR', 0.00);
INSERT INTO card (num, balance, currency_code, debit_frozen_balance)
VALUES (0004, 0.00, 'KZT', 0.00);

CREATE TABLE IF NOT EXISTS transaction
(
    id SERIAL PRIMARY KEY,
    from_card_id INTEGER,
    to_card_id INTEGER,
    amount DECIMAL(18, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(7) NOT NULL
);

set transaction isolation level serializable;
create or replace function create_transaction(amount numeric(18, 2), sender_card_number integer, recipient_card_number integer, currency_code varchar(3))
    returns integer
as
$$
declare
    id_transaction integer;
BEGIN
    -- Фиксируем транзакцию
    insert into transaction (amount, from_card_id, to_card_id, status, currency_code)
    values (amount, sender_card_number, recipient_card_number, 'Created', currency_code)returning id into id_transaction;
    return id_transaction;
END;
$$
    language plpgsql;
-- select create_transaction(500, 2,0, 'RUB') AS answer;


set transaction isolation level serializable;
create or replace function freeze_sender_balance(id_transaction integer, sender_amount DECIMAL(18, 2), sender_card_number integer)
    returns bool
as
$$
declare
    sender_balance DECIMAL(18, 2);
BEGIN
    -- Проверяем баланс плательщика
    select balance into sender_balance from card where num = sender_card_number;
    if sender_balance < sender_amount then
        -- Недостаточно средств на счете плательщика
        update transaction set status='Error' where id = id_transaction;
        return false;

    else
        -- Обновляем баланс плательщика
        update card set balance = balance - sender_amount where num = sender_card_number;
        -- Замораживаем сумму у плательщика
        update card set debit_frozen_balance = debit_frozen_balance + sender_amount where num = sender_card_number;
        return true;
    end if;
END;
$$
    language plpgsql;
-- select freeze_sender_balance(4,500, 2) AS answer;


set transaction isolation level serializable;
create or replace function increase_balance(id_transaction integer, sender_amount DECIMAL(18, 2),
recipient_amount DECIMAL(18, 2), sender_card_number integer, recipient_card_number integer)
    returns void
as
$$
BEGIN
    -- Обновляем баланс получателя
    update card set balance = balance + recipient_amount where num = recipient_card_number;
    -- Размораживаем сумму у плательщика
    update card set debit_frozen_balance = debit_frozen_balance - sender_amount where num = sender_card_number;
    update transaction set status='Success' where id = id_transaction;
END;
$$
    language plpgsql;
-- select increase_balance(1,500, 500, 2,0) AS answer;

