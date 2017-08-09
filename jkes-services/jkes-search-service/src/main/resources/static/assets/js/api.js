
$("#request_body_search_form").submit(function (event) {
    event.preventDefault();

    var target_container = $('#search_result_container');

    var $form = $(this),
        path = $form.find("#path").val(),
        request_body = $form.find("#request_body").val();

    var data;
    try {
        data = JSON.stringify(JSON.parse(request_body));
    }catch (error) {
        fillError(error);
        return
    }

    path = window.location.pathname + "/" + path;

    if(path.indexOf("_search") == -1) {
        $.get(path, function (dada) {
            fillResult(dada);
        }).fail(function (error) {
            fillError(error.responseText);
        });
    }else {
        $.ajax({
            url: path,
            type: 'post',
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                fillResult(data);
            },
            error: function (error) {
                fillError(error.responseText);
            },
            data: data
        });
    }

    var fillResult = function (data) {
        var content;
        try {
            content = JSON.stringify(data, null, 4);
        }catch (error) {
            fillError(error);
        }
        var code = $("<code contenteditable='true'/>").append(content);
        var pre = $("<pre/>").append(code);

        target_container.empty();
        target_container.append(
            pre
        );
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    };

    function fillError(error) {
        var content;
        try {
            content = JSON.stringify(JSON.parse(error), null, 4);
        }catch (error) {
            content = error;
        }

        var code = $("<code contenteditable='true'/>").append(content);
        var pre = $("<pre/>").append(code);

        target_container.empty();
        target_container.append(
            pre
        );
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    };
});

