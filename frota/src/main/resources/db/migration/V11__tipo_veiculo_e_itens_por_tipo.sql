-- Veículos agora podem ser CARRO ou MOTO (os que já existem continuam CARRO)
ALTER TABLE veiculos
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'CARRO';

-- Cada item da vistoria passa a dizer a que tipo de veículo se aplica
ALTER TABLE itens_vistoria
    ADD COLUMN aplica_a VARCHAR(10) NOT NULL DEFAULT 'AMBOS';

UPDATE itens_vistoria SET aplica_a = 'CARRO'
WHERE nome IN ('Teto', 'Pneu dianteiro esquerdo', 'Pneu dianteiro direito', 'Pneu traseiro esquerdo',
               'Pneu traseiro direito', 'Bancos e interior', 'Porta-malas', 'Step', 'Triangulo',
               'Macaco', 'Chave de roda');

INSERT INTO itens_vistoria (nome, categoria, obrigatorio, ordem, aplica_a) VALUES
    ('Pneu dianteiro',      'EXTERNO',   TRUE,  300, 'MOTO'),
    ('Pneu traseiro',       'EXTERNO',   TRUE,  310, 'MOTO'),
    ('Tanque e carenagens', 'EXTERNO',   TRUE,  320, 'MOTO'),
    ('Retrovisores',        'EXTERNO',   TRUE,  330, 'MOTO'),
    ('Banco',               'INTERNO',   TRUE,  340, 'MOTO'),
    ('Escapamento',         'EXTERNO',   TRUE,  350, 'MOTO'),
    ('Corrente e relacao',  'EXTERNO',   FALSE, 360, 'MOTO'),
    ('Bau ou bagageiro',    'ACESSORIO', FALSE, 370, 'MOTO'),
    ('Capacete',            'ACESSORIO', FALSE, 380, 'MOTO');
